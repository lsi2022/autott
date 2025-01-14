package org.acme.facilitylocation.solver;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumLong;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationConstraintConfiguration;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                facilityCapacity(constraintFactory),
                setupCost(constraintFactory),
                distanceFromFacility(constraintFactory)
        };
    }

    Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility, sumLong(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalizeConfigurableLong(
                        FacilityLocationConstraintConfiguration.FACILITY_CAPACITY,
                        (facility, demand) -> demand - facility.getCapacity());
    }

    Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility)
                .penalizeConfigurableLong(
                        FacilityLocationConstraintConfiguration.FACILITY_SETUP_COST,
                        Facility::getSetupCost);
    }

    Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .filter(Consumer::isAssigned)
                .penalizeConfigurableLong(
                        FacilityLocationConstraintConfiguration.DISTANCE_FROM_FACILITY,
                        Consumer::distanceFromFacility);
    }
}

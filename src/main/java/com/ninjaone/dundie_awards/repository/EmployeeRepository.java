package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByOrganization(Organization organization);

    @Query("SELECT SUM(e.dundieAwards) FROM Employee e")
    Long findTotalAwards();

    @Modifying
    @Query("""
            UPDATE Employee e
            SET e.dundieAwards = e.dundieAwards - :amount
            WHERE e.id IN :employeeIds AND e.dundieAwards >= :amount
           """)
    int decrementDundieAwards(@Param("employeeIds") List<Long> employeeIds, @Param("amount") int amount);
}

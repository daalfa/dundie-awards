package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDTO;
import com.ninjaone.dundie_awards.exception.NotFoundException;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmployeeService {

    private final OrganizationRepository organizationRepository;

    private final EmployeeRepository employeeRepository;

    public EmployeeService(
            final OrganizationRepository organizationRepository,
            final EmployeeRepository employeeRepository) {
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found by id: " + id));
    }

    public Employee createEmployee(EmployeeDTO employeeDto) {

        Organization organization = organizationRepository.findById(employeeDto.organizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found by id: " +
                        employeeDto.organizationId()));

        return employeeRepository.save(
                new Employee(employeeDto.firstName(), employeeDto.firstName(), organization));
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
}

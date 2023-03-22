package com.company.repository;

import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Long>
{

    User findUserById(Long id);

    User findUserByEmail(String email);

    List<User> findUsersByProjectsAssignedId(Long projectId);

    Page<User> findAllByOrderByFirstNameAsc(Pageable pageable);

    Page<User> findUsersByProjectsAssignedId(Long projectId, Pageable pageable);

    Page<User> findUsersByProjectsAssignedIdAndIdNot(Long projectId, Long userId, Pageable pageable);

    Page<User> findUsersByRoleNameOrderByFirstName(RoleEnum roleName, Pageable pageable);

    Page<User> findUsersByRoleNameAndIdNotInOrderByFirstName(RoleEnum roleName, List<Long> usersIds, Pageable pageable);

    @Query(value = "SELECT u FROM User u " +
                   "WHERE CONCAT(u.firstName, ' ' ,u.lastName) LIKE :fullName% " +
                   "OR CONCAT(u.lastName, ' ', u.firstName) LIKE :fullName% ORDER BY u.firstName")
    Page<User> findUserByFirstNameAndLastNameContaining(String fullName, Pageable pageable);


    @Query(value = "SELECT u FROM User u JOIN u.projectsAssigned project " +
                   "WHERE project.id = :projectId " +
                   "AND (CONCAT(u.firstName, ' ' ,u.lastName) LIKE :fullName% " +
                   "OR CONCAT(u.lastName, ' ', u.firstName) LIKE :fullName%)" +
                   "ORDER BY u.firstName")
    Page<User> findUsersByFullNameAndProjectsAssignedId(String fullName, Long projectId, Pageable pageable);

    @Query(value = "SELECT u FROM User u JOIN u.projectsAssigned project " +
                   "WHERE project.id = :projectId " +
                   "AND u.id <> :userId " +
                   "AND (CONCAT(u.firstName, ' ' ,u.lastName) LIKE :fullName% " +
                   "OR CONCAT(u.lastName, ' ', u.firstName) LIKE :fullName%)" +
                   "ORDER BY u.firstName")
    Page<User> findUsersByFullNameAndProjectsAssignedIdAndIdNot(Long projectId, Long userId, String fullName, Pageable pageable);

    @Query(value = "SELECT u FROM User u " +
            "WHERE u.role.name = :roleName " +
            "AND (CONCAT(u.firstName, ' ' ,u.lastName) LIKE :fullName% " +
            "OR CONCAT(u.lastName, ' ', u.firstName) LIKE :fullName%) " +
            "ORDER BY u.firstName")
    Page<User> findUsersByRoleAndFullName(RoleEnum roleName, String fullName, Pageable pageable);

    @Query(value = "SELECT u FROM User u " +
                   "WHERE u.id NOT IN :userIds " +
                   "AND u.role.name = :roleName " +
                   "AND (CONCAT(u.firstName, ' ' ,u.lastName) LIKE :fullName% " +
                   "OR CONCAT(u.lastName, ' ', u.firstName) LIKE :fullName%) " +
                   "ORDER BY u.firstName")
    Page<User> findUsersByRoleAndFullNameAndNotInProject(RoleEnum roleName, String fullName, List<Long> userIds, Pageable pageable);
}

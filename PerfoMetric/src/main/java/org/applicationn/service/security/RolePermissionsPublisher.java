package org.applicationn.service.security;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.applicationn.domain.security.RolePermission;
import org.applicationn.domain.security.UserRole;

@Singleton
@Startup
public class RolePermissionsPublisher {

    private static final Logger logger = Logger.getLogger(RolePermissionsPublisher.class.getName());
    
    @Inject
    private RolePermissionsService rolePermissionService;
    
    @PostConstruct
    public void postConstruct() {

        if (rolePermissionService.countAllEntries() == 0) {

            rolePermissionService.save(new RolePermission(UserRole.Administrator, "employees:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "employees:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "employees:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "employees:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "employees:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "employees:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "departements:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "departements:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "departements:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "departements:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "departements:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "departements:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "postes:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "postes:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "postes:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "postes:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "postes:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "postes:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "primes:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "primes:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "primes:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "primes:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "primes:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "primes:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "resultas:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "resultas:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "resultas:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "resultas:create"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "resultas:update"));
            
            rolePermissionService.save(new RolePermission(UserRole.superviseur, "resultas:delete"));
            
            rolePermissionService.save(new RolePermission(UserRole.Administrator, "user:*"));
            
            logger.info("Successfully created permissions for user roles.");
        }
    }
}

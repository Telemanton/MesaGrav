package antonio.mesa.antonio_mesa_gravimetrica;
/////////////////////////////////////////////////////
//////////////////////////////////////////////////
        // Role class documentation //
//////////////////////////////////////////////////
/////////////////////////////////////////////////////
/**
 * The Role class defines the different user roles who can exist in the application.
 * Each role has different permissions, responsibilities and ways to use within the application.
 * 
 * These defined roles are:
 * - KEYUSER:
 *   - Description: This role has access to key system functions. It is the second most privileged role after ADMIN.
 *   - Permissions: Can access key system functions, yet does not have access to user management functions.
 * - ADMIN:
 *   - Description: This role has the privileged of user management functions.
 *   - Permissions: Can access all system functions, including user management and key system functions.
 * - USER:
 *   - Description: This role has just limited access for data visualization and basic function usage.
 *   - Permissions: Can view data and use basic functions
 * This enumeration is used to control access to different parts of the application based on the user's role.
 */
public enum Role {
    KEYUSER,
    ADMIN,
    USER
}

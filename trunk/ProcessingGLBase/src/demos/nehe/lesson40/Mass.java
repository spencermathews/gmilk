/*
 * Mass.java
 * 
 * Created on 26/10/2007
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demos.nehe.lesson40;

/**
 * Java implementation of Erkin Tunca's "Mass" class from "Physics1.h" by Scott Rains.<br/>
 * Date: 26 Oct 2007
 *
 * @author Scott Rains (sid.rains[at]gmail.com)
 */
public class Mass {
    float m;                                    // The mass value
    Vector3D pos;                                // Position in space
    Vector3D vel;                                // Velocity
    Vector3D force;                                // Force applied on this mass at an instance

    /**
     * Default constructor. Zeroed inital values for position, velocity and force.
     *
     * @param m Initial mass
     */
    public Mass(float m) {                            // Constructor
        this.m = m;
        pos = new Vector3D();
        vel = new Vector3D();
        force = new Vector3D();
    }

    /**
     * void applyForce(Vector3D force) method is used to add external force to the mass.
     * At an instance in time, several sources of force might affect the mass. The vector sum
     * of these forces make up the net force applied to the mass at the instance.
     * *param force Force vector to apply
     */
    public void applyForce(Vector3D force) {
        this.force.addTo(force);                                                // The external force is added to the force of the mass
    }

    /**
     * void init() method sets the force values to zero
     */
    public void init() {
        force.x = 0;
        force.y = 0;
        force.z = 0;
    }

    /**
     * void simulate(float dt) method calculates the new velocity and new position of
     * the mass according to change in time (dt). Here, a simulation method called
     * "The Euler Method" is used. The Euler Method is not always accurate, but it is
     * simple. It is suitable for most of physical simulations that we know in common
     * computer and video games.
     * *param dt Delta time
     */
    public void simulate(float dt) {
        vel.addTo(force.divide(m).multiply(dt));                // Change in velocity is added to the velocity.
        // The change is proportinal with the acceleration (forceScaled / m) and change in time
        pos.addTo(vel.multiply(dt));                        // Change in position is added to the position.
        // Change in position is velocity times the change in time
    }
}

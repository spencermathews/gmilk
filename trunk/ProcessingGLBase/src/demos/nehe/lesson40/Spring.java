/*
 * Spring.java
 * 
 * Created on 26/10/2007
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demos.nehe.lesson40;

/**
 * Java implementation of Erkin Tunca's "Spring" class from "Physics2.h" by Scott Rains.<br/>
 * Date: 26 Oct 2007
 *
 * @author Scott Rains (sid.rains[at]gmail.com)
 */

public class Spring {
    Mass mass1;                                // The First Mass At One Tip Of The Spring
    Mass mass2;                                // The Second Mass At The Other Tip Of The Spring

    float springConstant;                            // A Constant To Represent The Stiffness Of The Spring
    float springLength;                            // The Length That The spring Does Not Exert Any Force
    float frictionConstant;                            // A Constant To be Used For The Inner Friction Of The Spring

    public Spring(Mass mass1, Mass mass2, float springConstant, float springLength, float frictionConstant) {
        this.springConstant = springConstant;                // Set The springConstant
        this.springLength = springLength;                // Set The springLength
        this.frictionConstant = frictionConstant;            // Set The frictionConstant

        this.mass1 = mass1;                        // Set mass1
        this.mass2 = mass2;                        // Set mass2
    }

    public void solve() {                            // solve() Method: The Method Where Forces Can Be Applied
        Vector3D springVector = mass1.pos.subtract(mass2.pos);        // Vector Between The Two Masses

        float r = springVector.length();                // Distance Between The Two Masses

        Vector3D force = new Vector3D();                // Force Initially Has A Zero Value

        if (r != 0) {                            // To Avoid A Division By Zero... Check If r Is Zero
            // The Spring Force Is Added To The Force
            Vector3D temp = springVector.divide(r).multiply((r - springLength) * (-springConstant));
            force.addTo(temp);
        }
        Vector3D fictionForce = mass1.vel.subtract(mass2.vel);
        fictionForce.multiplyTo(-frictionConstant);
        force.addTo(fictionForce);                                      // The Friction Force Is Added To The force
        // With This Addition We Obtain The Net Force Of The Spring
        mass1.applyForce(force);                    // Force Is Applied To mass1
        mass2.applyForce(force.negative());                // The Opposite Of Force Is Applied To mass2
    }                                    // Void Solve() Ends Here
}

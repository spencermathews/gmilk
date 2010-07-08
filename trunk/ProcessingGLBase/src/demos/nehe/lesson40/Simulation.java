/*
 * Simulation.java
 * 
 * Created on 26/10/2007
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demos.nehe.lesson40;

/**
 * Java implementation of Erkin Tunca's "Simulation" class from "Physics1.h" by Scott Rains.<br/>
 * Date: 26 Oct 2007
 *
 * @author Scott Rains (sid.rains[at]gmail.com)
 */
public class Simulation {
    int numOfMasses;                            // number of masses in this container
    protected Mass[] masses;                                                // masses are held by pointer to pointer. (Here Mass** represents a 1 dimensional array)

    public Simulation(int numOfMasses, float m) {
        this.numOfMasses = numOfMasses;
        masses = new Mass[numOfMasses];                                 // Create an array of pointers
        for (int a = 0; a < numOfMasses; a++) {                         // We Will Step To Every Pointer In The Array.
            masses[a] = new Mass(m);                                // Create A Mass As A Pointer And Put It In The Array.
        }
    }

    public Mass getMass(int index) {
        if (index < 0 || index >= numOfMasses)                          // if the index is not in the array
        {
            return null;                        // then return NULL
        }

        return masses[index];                        // get the mass at the index
    }

    public void init() {
        for (int a = 0; a < numOfMasses; ++a) {                         // We will init() every mass
            masses[a].init();                                       // call init() method of the mass
        }
    }

    public void solve() {
        // no implementation because no forces are wanted in this basic container
        // in advanced containers, this method will be overridden and some forces will act on masses
    }

    public void simulate(float dt) {
        for (int a = 0; a < numOfMasses; ++a) {                         // We will iterate every mass
            masses[a].simulate(dt);                                 // Iterate the mass and obtain new position and new velocity
        }
    }

    public void operate(float dt) {
        init();                                                         // Step 1: reset forces to zero
        solve();                                                        // Step 2: apply forces
        simulate(dt);                                                   // Step 3: iterate the masses by the change in time
    }
}
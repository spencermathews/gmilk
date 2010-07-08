/*
 * RopeSimulation.java
 *
 * Created on 26/10/2007
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demos.nehe.lesson40;

/**
 * Java implementation of Erkin Tunca's "RopeSimulation" class from "Physics2.h" by Scott Rains.<br/>
 * <p>Quote from Erkin Tunca's code...<br/>
 * class RopeSimulation is derived from class Simulation (see Physics1.h). It simulates a rope with
 * point-like particles binded with springs. The springs have inner friction and normal length. One tip of
 * the rope is stabilized at a point in space called "Vector3D ropeConnectionPos". This point can be
 * moved externally by a method "void setRopeConnectionVel(Vector3D ropeConnectionVel)". RopeSimulation
 * creates air friction and a planer surface (or ground) with a normal in +y direction. RopeSimulation
 * implements the force applied by this surface. In the code, the surface is refered as "ground".</p>
 * Date: 26 Oct 2007
 *
 * @author Scott Rains (sid.rains[at]gmail.com)
 */
public class RopeSimulation extends Simulation {

    private Spring[] springs;                                               // Springs Binding The Masses (There Shall Be [numOfMasses - 1] Of Them)
    private final Vector3D gravitation;                                     // Gravitational Acceleration (Gravity Will Be Applied To All Masses)
    private final Vector3D ropeConnectionPos;                               // A Point In Space That Is Used To Set The Position Of The
    // First Mass In The System (Mass With Index 0)
    private final Vector3D ropeConnectionVel;                               // A Variable To Move The ropeConnectionPos (By This, We Ccan Swing The Rope)
    private float groundRepulsionConstant;                                  // A Constant To Represent How Much The Ground Shall Repel The Masses
    private float groundFrictionConstant;                                   // A Constant Of Friction Applied To Masses By The Ground
    // (Used For Sliding Of Rope On The Ground)
    private float groundAbsorptionConstant;                                 // A Constant Of Absorption Friction Applied To Masses By The Ground
    // (Used For Vertical Collisions Of The Rope With The Ground)
    public float groundHeight;                                              // A Value To Represent The Y Value Of The Ground
    // (The Ground Is A Planer Surface Facing +Y Direction)
    private float airFrictionConstant;                                      // A Constant Of Air Friction Applied To Masses}

    /**
     * Full constructor
     *
     * @param numOfMasses              The Number Of Masses
     * @param m                        Weight Of Each Mass
     * @param springConstant           How Stiff The Springs Are
     * @param springLength             The Length That A Spring Does Not Exert Any Force
     * @param springFrictionConstant   Inner Friction Constant Of Spring
     * @param gravitation              Gravitational Acceleration
     * @param airFrictionConstant      Air Friction Constant
     * @param groundRepulsionConstant  Ground Repulsion Constant
     * @param groundFrictionConstant   Ground Friction Constant
     * @param groundAbsorptionConstant Ground Absorption Constant
     * @param groundHeight             Height Of The Ground (Y Position)
     */
    public RopeSimulation(int numOfMasses,                                  // 1. The Number Of Masses
                          float m,                                                        // 2. Weight Of Each Mass
                          float springConstant,                                           // 3. How Stiff The Springs Are
                          float springLength,                                             // 4. The Length That A Spring Does Not Exert Any Force
                          float springFrictionConstant,                                   // 5. Inner Friction Constant Of Spring
                          Vector3D gravitation,                                           // 6. Gravitational Acceleration
                          float airFrictionConstant,                                      // 7. Air Friction Constant
                          float groundRepulsionConstant,                                  // 8. Ground Repulsion Constant
                          float groundFrictionConstant,                                   // 9. Ground Friction Constant
                          float groundAbsorptionConstant,                                 // 10. Ground Absorption Constant
                          float groundHeight) {

        super(numOfMasses, m);                                          // The Super Class Creates Masses With Weights m Of Each
        ropeConnectionPos = new Vector3D();
        ropeConnectionVel = new Vector3D();

        this.gravitation = new Vector3D(gravitation);

        this.airFrictionConstant = airFrictionConstant;

        this.groundFrictionConstant = groundFrictionConstant;
        this.groundRepulsionConstant = groundRepulsionConstant;
        this.groundAbsorptionConstant = groundAbsorptionConstant;
        this.groundHeight = groundHeight;

        for (int a = 0; a < numOfMasses; ++a)
        {                         // To Set The Initial Positions Of Masses Loop With For(;;)
            masses[a].pos.x = a * springLength;                     // Set X-Position Of masses[a] With springLength Distance To Its Neighbor
            masses[a].pos.y = 0;                                    // Set Y-Position As 0 So That It Stand Horizontal With Respect To The Ground
            masses[a].pos.z = 0;                                    // Set Z-Position As 0 So That It Looks Simple
        }

        springs = new Spring[numOfMasses - 1];                          // Create [numOfMasses - 1] Pointers For springs
        // ([numOfMasses - 1] Springs Are Necessary For numOfMasses)
        for (int a = 0; a < numOfMasses - 1; ++a) {                     //to create each spring, start a loop
            // Create The Spring With Index "a" By The Mass With Index "a" And Another Mass With Index "a + 1".
            springs[a] = new Spring(masses[a], masses[a + 1], springConstant, springLength, springFrictionConstant);
        }
    }

    public void solve() {                                                   // solve() Is Overriden Because We Have Forces To Be Applied
        for (int a = 0; a < numOfMasses - 1; ++a) {                     // Apply Force Of All Springs
            springs[a].solve();                                     // Spring With Index "a" Should Apply Its Force
        }

        for (int a = 0; a < numOfMasses; ++a) {
            masses[a].applyForce(gravitation.multiply(masses[a].m));                // The Gravitational Force
            masses[a].applyForce(masses[a].vel.multiply(-airFrictionConstant));     // The air friction
            if (masses[a].pos.y < groundHeight) {
                Vector3D v = new Vector3D();                                    //A temporary Vector3D
                v.set(masses[a].vel);                                           // Get The Velocity
                v.y = 0;                                                        // Omit The Velocity Component In Y-Direction

                // The Velocity In Y-Direction Is Omited Because We Will Apply A Friction Force To Create
                // A Sliding Effect. Sliding Is Parallel To The Ground. Velocity In Y-Direction Will Be Used
                // In The Absorption Effect.
                // Ground Friction Force Is Applied
                masses[a].applyForce(v.multiply(-groundFrictionConstant));

                v.set(masses[a].vel);                           // Get The Velocity
                v.x = 0;                                        // Omit The x And z Components Of The Velocity
                v.z = 0;                                        // We Will Use v In The Absorption Effect
                // Above, We Obtained A Velocity Which Is Vertical To The Ground And It Will Be Used In
                // The Absorption Force
                if (v.y < 0) {
                    // Let's Absorb Energy Only When A Mass Collides Towards The Ground
                    // The Absorption Force Is Applied
                    masses[a].applyForce(v.multiply(-groundAbsorptionConstant));
                }
                // The Ground Shall Repel A Mass Like A Spring.
                // By "Vector3D(0, groundRepulsionConstant, 0)" We Create A Vector In The Plane Normal Direction
                // With A Magnitude Of groundRepulsionConstant.
                // By (groundHeight - masses[a]->pos.y) We Repel A Mass As Much As It Crashes Into The Ground.
                Vector3D force = new Vector3D(0f, groundRepulsionConstant, 0f).multiply(groundHeight - masses[a].pos.y);

                masses[a].applyForce(force);                    // The Ground Repulsion Force Is Applied
            }
        }
    }

    public void simulate(float dt) {                                        // simulate(float dt) Is Overriden Because We Want To Simulate the Motion Of The ropeConnectionPos
        super.simulate(dt);                                             // The Super Class Shall Simulate The Masses
        ropeConnectionPos.addTo(ropeConnectionVel.multiply(dt));        // Iterate The Positon Of ropeConnectionPos
        if (ropeConnectionPos.y < groundHeight) {                       // ropeConnectionPos Shall Not Go Under The Ground
            ropeConnectionPos.y = groundHeight;
            ropeConnectionVel.y = 0;
        }

        masses[0].pos.set(ropeConnectionPos);                           // Mass With Index "0" Shall Position At ropeConnectionPos
        masses[0].vel.set(ropeConnectionVel);                           // The Mass's Velocity Is Set To Be Equal To ropeConnectionVel
    }

    /**
     * The Method To Set ropeConnectionVel
     */
    public void setRopeConnectionVel(Vector3D v) {
        ropeConnectionVel.set(v);
    }
}
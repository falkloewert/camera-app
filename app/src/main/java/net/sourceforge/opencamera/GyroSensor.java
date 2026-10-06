package net.sourceforge.opencamera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** Handles gyro sensor.
 */
public class GyroSensor implements SensorEventListener {
    private static final String TAG = "GyroSensor";

    final private SensorManager mSensorManager;
    final private Sensor mSensor;
    final private Sensor mSensorAccel;

    private boolean is_recording;
    private long timestamp;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float [] deltaRotationVector = new float[4];
    private boolean has_gyroVector;
    private final float [] gyroVector = new float[3];
    private final float [] currentRotationMatrix = new float[9];
    private final float [] currentRotationMatrixGyroOnly = new float[9];
    private final float [] deltaRotationMatrix = new float[9];
    private final float [] tempMatrix = new float[9];
    private final float [] temp2Matrix = new float[9];

    private boolean has_init_accel = false;
    private final float [] initAccelVector = new float[3];
    private final float [] accelVector = new float[3];

    // Falk 2026-09-25: Blickrichtung beim Start des Panoramas. Upstream setzte die Zielpunkte in
    // die Horizontalebene des Bezugssystems (y = 0), d.h. in die Ebene senkrecht zur Hoch-Achse des
    // Handys beim Start. Bei schräg begonnenem Panorama ist das nicht die Horizontebene der Welt:
    // die Führung zog die Aufnahme damit im Verlauf zum Horizont zurück, statt die begonnene
    // Neigung zu halten. Gebraucht wird deshalb die Erdvertikale (initAccelVector, siehe
    // getUpVector()) und die Startblickrichtung.
    private final float [] startVector = new float[3];
    private boolean has_start_vector;
    // Die Rolllage (is_upright) und die Neigung werden getrennt geprüft: is_upright behält seine
    // Bedeutung für den Dreh-Hinweis im Sucher (zu weit im/gegen den Uhrzeigersinn), die Neigung
    // braucht einen eigenen Wert, weil ein schräg begonnenes Panorama sie halten soll statt sie
    // aufzugeben. pitch_ok heißt beim senkrechten Schwenk eigentlich "Gieren konstant" - die
    // Neigung (Pitch) ändert sich dort ja absichtlich.
    private boolean pitch_ok = true;

    // Ob der Schwenk senkrecht (um die Querachse) statt waagerecht (um die Erdvertikale) läuft.
    // Bestimmt, welche der beiden Bedingungen in onSensorChanged() die Aufnahme freigibt.
    private boolean panorama_vertical;

    private boolean has_original_rotation_matrix;
    private final float [] originalRotationMatrix = new float[9];
    private boolean has_rotationVector;
    private final float [] rotationVector = new float[3];

    // temporary vectors:
    private final float [] tempVector = new float[3];
    private final float [] inVector = new float[3];
    private final float [] upVector = new float[3];
    private final float [] startVectorTmp = new float[3];

    public interface TargetCallback {
        /** Called when the target has been achieved.
         * @param indx Index of the target that has been achieved.
         */
        void onAchieved(int indx);
        /* Called when the orientation is significantly far from the target.
         */
        void onTooFar();
    }

    private boolean hasTarget;
    //private final float [] targetVector = new float[3];
    private final List<float []> targetVectors = new ArrayList<>();
    private float targetAngle; // target angle in radians
    private float uprightAngleTol; // in radians
    private boolean targetAchieved;
    private float tooFarAngle; // in radians
    private TargetCallback targetCallback;
    private boolean has_lastTargetAngle;
    private float lastTargetAngle;
    private int is_upright; // if hasTarget==true, this stores whether the "upright" orientation of the device is close enough to the orientation when recording was started: 0 for yes, otherwise -1 for too anti-clockwise, +1 for too clockwise

    GyroSensor(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        //mSensorAccel = null;

        if( MyDebug.LOG ) {
            Log.d(TAG, "GyroSensor");
            if( mSensor == null )
                Log.d(TAG, "gyroscope not available");
            else if( mSensorAccel == null )
                Log.d(TAG, "accelerometer not available");
        }
        setToIdentity();
    }

    boolean hasSensors() {
        // even though the gyro sensor works if mSensorAccel is not present, for best behaviour we require them both
        return mSensor != null && mSensorAccel != null;
    }

    private void setToIdentity() {
        for(int i=0;i<9;i++) {
            currentRotationMatrix[i] = 0.0f;
        }
        currentRotationMatrix[0] = 1.0f;
        currentRotationMatrix[4] = 1.0f;
        currentRotationMatrix[8] = 1.0f;
        System.arraycopy(currentRotationMatrix, 0, currentRotationMatrixGyroOnly, 0, 9);

        for(int i=0;i<3;i++) {
            initAccelVector[i] = 0.0f;
            // don't set accelVector, rotationVector, gyroVector to 0 here, as we continually smooth the values even when not recording
        }
        has_init_accel = false;
        has_original_rotation_matrix = false;
        has_start_vector = false;
    }

    /** Direction the device is currently pointing in, in the coordinate system of startRecording().
     *  Without any movement since the start of the recording this is (0, 0, -1).
     * @param out An array of length 3 to store the returned vector.
     */
    public void getViewVector(float [] out) {
        setVector(inVector, 0.0f, 0.0f, -1.0f); // vector pointing behind the device's screen
        transformVector(out, currentRotationMatrix, inVector);
    }

    /** The world's "up" (gravity) in the coordinate system of startRecording(), normalised.
     *  Deliberately not simply (0, 1, 0): that axis is the device's own up at the moment the
     *  recording started, so for a device that started out tilted the two differ.
     * @param out An array of length 3 to store the returned vector.
     */
    public void getUpVector(float [] out) {
        if( has_init_accel ) {
            System.arraycopy(initAccelVector, 0, out, 0, 3);
        }
        else {
            // noch kein Beschleunigungswert: "gerade gehalten" annehmen, das ist das Verhalten von vorher
            setVector(out, 0.0f, 1.0f, 0.0f);
        }
    }

    /** The direction the device was pointing in when the current target was first set, in the
     *  coordinate system of startRecording().
     * @param out An array of length 3 to store the returned vector.
     */
    void getStartVector(float [] out) {
        if( has_start_vector ) {
            System.arraycopy(startVector, 0, out, 0, 3);
        }
        else {
            getViewVector(out);
        }
    }

    /** Whether the device keeps the tilt (pitch) it had when the current target was first set.
     *  Together with isUpright() (the roll) this is what has to hold for a panorama to go in a
     *  circle at a constant tilt.
     */
    boolean isPitchOk() {
        return this.pitch_ok;
    }

    /** Helper method to set a 3D vector.
     */
    static void setVector(final float[] vector, float x, float y, float z) {
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }

    /** Helper method to access the (i, j)th component of a 3x3 matrix.
     */
    private static float getMatrixComponent(final float [] matrix, int row, int col) {
        return matrix[row*3+col];
    }

    /** Helper method to set the (i, j)th component of a 3x3 matrix.
     */
    private static void setMatrixComponent(final float [] matrix, int row, int col, float value) {
        matrix[row*3+col] = value;
    }

    /** Helper method to multiply 3x3 matrix with a 3D vector.
     */
    public static void transformVector(final float [] result, final float [] matrix, final float [] vector) {
        // result[i] = matrix[ij] . vector[j]
        for(int i=0;i<3;i++) {
            result[i] = 0.0f;
            for(int j=0;j<3;j++) {
                result[i] += getMatrixComponent(matrix, i, j) * vector[j];
            }
        }
    }

    /** Helper method to multiply the transpose of a 3x3 matrix with a 3D vector.
     *  For 3x3 rotation (orthonormal) matrices, the transpose is the inverse.
     */
    private void transformTransposeVector(final float [] result, final float [] matrix, final float [] vector) {
        // result[i] = matrix[ji] . vector[j]
        for(int i=0;i<3;i++) {
            result[i] = 0.0f;
            for(int j=0;j<3;j++) {
                result[i] += getMatrixComponent(matrix, j, i) * vector[j];
            }
        }
    }

    /* We should enable sensors before startRecording(), so that we can apply smoothing to the
     * sensors to reduce noise.
     * This should be limited to when we might want to use the gyro, to help battery life.
     */
    void enableSensors() {
        if( MyDebug.LOG )
            Log.d(TAG, "enableSensors");
        has_rotationVector = false;
        has_gyroVector = false;
        for(int i=0;i<3;i++) {
            accelVector[i] = 0.0f;
            rotationVector[i] = 0.0f;
            gyroVector[i] = 0.0f;
        }

        if( mSensor != null )
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        if( mSensorAccel != null )
            mSensorManager.registerListener(this, mSensorAccel, SensorManager.SENSOR_DELAY_UI);
    }

    void disableSensors() {
        if( MyDebug.LOG )
            Log.d(TAG, "disableSensors");
        mSensorManager.unregisterListener(this);
    }

    void startRecording() {
        if( MyDebug.LOG )
            Log.d(TAG, "startRecording");
        is_recording = true;
        timestamp = 0;
        setToIdentity();
    }

    void stopRecording() {
        if( is_recording ) {
            if( MyDebug.LOG )
                Log.d(TAG, "stopRecording");
            is_recording = false;
            timestamp = 0;
        }
    }

    public boolean isRecording() {
        return this.is_recording;
    }

    void setTarget(float target_x, float target_y, float target_z, float targetAngle, float uprightAngleTol, float tooFarAngle, boolean vertical, TargetCallback targetCallback) {
        this.hasTarget = true;
        this.targetVectors.clear();
        addTarget(target_x, target_y, target_z);
        this.targetAngle = targetAngle;
        this.uprightAngleTol = uprightAngleTol;
        this.tooFarAngle = tooFarAngle;
        this.targetCallback = targetCallback;
        this.panorama_vertical = vertical;
        this.has_lastTargetAngle = false;
        this.lastTargetAngle = 0.0f;
        if( !has_start_vector ) {
            // die Blickrichtung, mit der das Panorama beginnt: die Neigung daraus soll es halten
            getViewVector(startVector);
            has_start_vector = true;
        }
    }

    void addTarget(float target_x, float target_y, float target_z) {
        float [] vector = new float[]{target_x, target_y, target_z};
        this.targetVectors.add(vector);
    }

    void clearTarget() {
        this.hasTarget = false;
        this.targetVectors.clear();
        this.targetCallback = null;
        this.has_lastTargetAngle = false;
        this.lastTargetAngle = 0.0f;
    }

    void disableTargetCallback() {
        this.targetCallback = null;
    }

    boolean hasTarget() {
        return this.hasTarget;
    }

    boolean isTargetAchieved() {
        return this.hasTarget && this.targetAchieved;
    }

    public int isUpright() {
        return this.is_upright;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void adjustGyroForAccel() {
        if( timestamp == 0 ) {
            // don't have a gyro matrix yet
            return;
        }
        else if( !has_init_accel ) {
            return;
        }
        /*if( true )
            return;*/ // don't use accelerometer for now

        //transformVector(tempVector, currentRotationMatrix, initAccelVector);
        // tempVector is now the initAccelVector transformed by the gyro matrix
        //transformTransposeVector(tempVector, currentRotationMatrix, initAccelVector);
        transformVector(tempVector, currentRotationMatrix, accelVector);
        // tempVector is now the accelVector transformed by the gyro matrix
        double cos_angle = (tempVector[0] * initAccelVector[0] + tempVector[1] * initAccelVector[1] + tempVector[2] * initAccelVector[2]);
        /*if( MyDebug.LOG ) {
            Log.d(TAG, "adjustGyroForAccel:");
            Log.d(TAG, "### currentRotationMatrix row 0: " + currentRotationMatrix[0] + " , " + currentRotationMatrix[1] + " , " + currentRotationMatrix[2]);
            Log.d(TAG, "### currentRotationMatrix row 1: " + currentRotationMatrix[3] + " , " + currentRotationMatrix[4] + " , " + currentRotationMatrix[5]);
            Log.d(TAG, "### currentRotationMatrix row 2: " + currentRotationMatrix[6] + " , " + currentRotationMatrix[7] + " , " + currentRotationMatrix[8]);
            Log.d(TAG, "### initAccelVector: " + initAccelVector[0] + " , " + initAccelVector[1] + " , " + initAccelVector[2]);
            Log.d(TAG, "### accelVector: " + accelVector[0] + " , " + accelVector[1] + " , " + accelVector[2]);
            Log.d(TAG, "### tempVector: " + tempVector[0] + " , " + tempVector[1] + " , " + tempVector[2]);
            Log.d(TAG, "### cos_angle: " + cos_angle);
        }*/
        if( cos_angle >= 0.99999999995 ) {
            // gyroscope already matches accelerometer
            return;
        }

        double angle = Math.acos(cos_angle);
        angle *= 0.02f; // filter
        cos_angle = Math.cos(angle);

        /*
        // compute matrix to transform tempVector to accelVector
        // compute (tempVector X accelVector) normalised
        double a_x = tempVector[1] * accelVector[2] - tempVector[2] * accelVector[1];
        double a_y = tempVector[2] * accelVector[0] - tempVector[0] * accelVector[2];
        double a_z = tempVector[0] * accelVector[1] - tempVector[1] * accelVector[0];
        */
        // compute matrix to transform tempVector to initAccelVector
        // compute (tempVector X initAccelVector) normalised
        double a_x = tempVector[1] * initAccelVector[2] - tempVector[2] * initAccelVector[1];
        double a_y = tempVector[2] * initAccelVector[0] - tempVector[0] * initAccelVector[2];
        double a_z = tempVector[0] * initAccelVector[1] - tempVector[1] * initAccelVector[0];
        double a_mag = Math.sqrt(a_x*a_x + a_y*a_y + a_z*a_z);
        if( a_mag < 1.0e-5 ) {
            // parallel or anti-parallel case
            return;
        }
        a_x /= a_mag;
        a_y /= a_mag;
        a_z /= a_mag;
        double sin_angle = Math.sqrt(1.0-cos_angle*cos_angle);
        // from http://immersivemath.com/forum/question/rotation-matrix-from-one-vector-to-another/
        setMatrixComponent(tempMatrix, 0, 0, (float)(a_x*a_x*(1.0-cos_angle)+cos_angle));
        setMatrixComponent(tempMatrix, 0, 1, (float)(a_x*a_y*(1.0-cos_angle)-sin_angle*a_z));
        setMatrixComponent(tempMatrix, 0, 2, (float)(a_x*a_z*(1.0-cos_angle)+sin_angle*a_y));
        setMatrixComponent(tempMatrix, 1, 0, (float)(a_x*a_y*(1.0-cos_angle)+sin_angle*a_z));
        setMatrixComponent(tempMatrix, 1, 1, (float)(a_y*a_y*(1.0-cos_angle)+cos_angle));
        setMatrixComponent(tempMatrix, 1, 2, (float)(a_y*a_z*(1.0-cos_angle)-sin_angle*a_x));
        setMatrixComponent(tempMatrix, 2, 0, (float)(a_x*a_z*(1.0-cos_angle)-sin_angle*a_y));
        setMatrixComponent(tempMatrix, 2, 1, (float)(a_y*a_z*(1.0-cos_angle)+sin_angle*a_x));
        setMatrixComponent(tempMatrix, 2, 2, (float)(a_z*a_z*(1.0-cos_angle)+cos_angle));
        /*if( MyDebug.LOG ) {
            // test:
            System.arraycopy(tempVector, 0, inVector, 0, 3);
            transformVector(tempVector, tempMatrix, inVector);
            Log.d(TAG, "### tempMatrix row 0: " + tempMatrix[0] + " , " + tempMatrix[1] + " , " + tempMatrix[2]);
            Log.d(TAG, "### tempMatrix row 1: " + tempMatrix[3] + " , " + tempMatrix[4] + " , " + tempMatrix[5]);
            Log.d(TAG, "### tempMatrix row 2: " + tempMatrix[6] + " , " + tempMatrix[7] + " , " + tempMatrix[8]);
            Log.d(TAG, "### rotated tempVector: " + tempVector[0] + " , " + tempVector[1] + " , " + tempVector[2]);
        }*/
        // replace currentRotationMatrix with tempMatrix.currentRotationMatrix
        // since [tempMatrix.currentRotationMatrix].[initAccelVector] = tempMatrix.tempVector = accelVector
        // since [tempMatrix.currentRotationMatrix].[accelVector] = tempMatrix.tempVector = initAccelVector
        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                float value = 0.0f;
                // temp2Matrix[ij] = tempMatrix[ik] * currentRotationMatrix[kj]
                for(int k=0;k<3;k++) {
                    value += getMatrixComponent(tempMatrix, i, k) * getMatrixComponent(currentRotationMatrix, k, j);
                }
                setMatrixComponent(temp2Matrix, i, j, value);
            }
        }

        System.arraycopy(temp2Matrix, 0, currentRotationMatrix, 0, 9);

        /*if( MyDebug.LOG ) {
            // test:
            //transformVector(tempVector, temp2Matrix, initAccelVector);
            //transformTransposeVector(tempVector, currentRotationMatrix, initAccelVector);
            transformVector(tempVector, temp2Matrix, accelVector);
            Log.d(TAG, "### new currentRotationMatrix row 0: " + temp2Matrix[0] + " , " + temp2Matrix[1] + " , " + temp2Matrix[2]);
            Log.d(TAG, "### new currentRotationMatrix row 1: " + temp2Matrix[3] + " , " + temp2Matrix[4] + " , " + temp2Matrix[5]);
            Log.d(TAG, "### new currentRotationMatrix row 2: " + temp2Matrix[6] + " , " + temp2Matrix[7] + " , " + temp2Matrix[8]);
            Log.d(TAG, "### new tempVector: " + tempVector[0] + " , " + tempVector[1] + " , " + tempVector[2]);
        }*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if( MyDebug.LOG )
            Log.d(TAG, "onSensorChanged: " + event);*/
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            final float sensor_alpha = 0.8f; // for filter
            for(int i=0;i<3;i++) {
                //this.accelVector[i] = event.values[i];
                this.accelVector[i] = sensor_alpha * this.accelVector[i] + (1.0f-sensor_alpha) * event.values[i];
            }

            double mag = Math.sqrt(accelVector[0]*accelVector[0] + accelVector[1]*accelVector[1] + accelVector[2]*accelVector[2]);
            if( mag > 1.0e-8 ) {
                //noinspection lossy-conversions
                accelVector[0] /= mag;
                //noinspection lossy-conversions
                accelVector[1] /= mag;
                //noinspection lossy-conversions
                accelVector[2] /= mag;
            }

            if( !has_init_accel ) {
                System.arraycopy(accelVector, 0, initAccelVector, 0, 3);
                has_init_accel = true;
            }

            adjustGyroForAccel();
        }
        else if( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ) {
            if( has_gyroVector ) {
                final float sensor_alpha = 0.5f; // for filter
                for(int i=0;i<3;i++) {
                    //this.gyroVector[i] = event.values[i];
                    this.gyroVector[i] = sensor_alpha * this.gyroVector[i] + (1.0f-sensor_alpha) * event.values[i];
                }
            }
            else {
                System.arraycopy(event.values, 0, this.gyroVector, 0, 3);
                has_gyroVector = true;
            }

            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if( timestamp != 0 ) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = gyroVector[0];
                float axisY = gyroVector[1];
                float axisZ = gyroVector[2];

                // Calculate the angular speed of the sample
                double omegaMagnitude = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if( omegaMagnitude > 1.0e-5 ) {
                    //noinspection lossy-conversions
                    axisX /= omegaMagnitude;
                    //noinspection lossy-conversions
                    axisY /= omegaMagnitude;
                    //noinspection lossy-conversions
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                double thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
                /*if( MyDebug.LOG ) {
                    Log.d(TAG, "### values: " + event.values[0] + " , " + event.values[1] + " , " + event.values[2]);
                    Log.d(TAG, "smoothed values: " + gyroVector[0] + " , " + gyroVector[1] + " , " + gyroVector[2]);
                }*/

                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                // User code should concatenate the delta rotation we computed with the current rotation
                // in order to get the updated rotation.
                // currentRotationMatrix = currentRotationMatrix * deltaRotationMatrix;
                for(int i=0;i<3;i++) {
                    for(int j=0;j<3;j++) {
                        float value = 0.0f;
                        // tempMatrix[ij] = currentRotationMatrix[ik] * deltaRotationMatrix[kj]
                        for(int k=0;k<3;k++) {
                            value += getMatrixComponent(currentRotationMatrix, i, k) * getMatrixComponent(deltaRotationMatrix, k, j);
                        }
                        setMatrixComponent(tempMatrix, i, j, value);
                    }
                }

                System.arraycopy(tempMatrix, 0, currentRotationMatrix, 0, 9);

                for(int i=0;i<3;i++) {
                    for(int j=0;j<3;j++) {
                        float value = 0.0f;
                        // tempMatrix[ij] = currentRotationMatrixGyroOnly[ik] * deltaRotationMatrix[kj]
                        for(int k=0;k<3;k++) {
                            value += getMatrixComponent(currentRotationMatrixGyroOnly, i, k) * getMatrixComponent(deltaRotationMatrix, k, j);
                        }
                        setMatrixComponent(tempMatrix, i, j, value);
                    }
                }
                System.arraycopy(tempMatrix, 0, currentRotationMatrixGyroOnly, 0, 9);


                /*if( MyDebug.LOG ) {
                    setVector(inVector, 0.0f, 0.0f, -1.0f); // vector pointing behind the device's screen
                    transformVector(tempVector, currentRotationMatrix, inVector);
                    //transformTransposeVector(tempVector, currentRotationMatrix, inVector);
                    Log.d(TAG, "### gyro vector: " + tempVector[0] + " , " + tempVector[1] + " , " + tempVector[2]);
                }*/

                adjustGyroForAccel();

            }

            timestamp = event.timestamp;
        }
        else if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR || event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR ) {
            if( has_rotationVector ) {
                //final float sensor_alpha = 0.7f; // for filter
                final float sensor_alpha = 0.8f; // for filter
                for(int i=0;i<3;i++) {
                    //this.rotationVector[i] = event.values[i];
                    this.rotationVector[i] = sensor_alpha * this.rotationVector[i] + (1.0f-sensor_alpha) * event.values[i];
                }
            }
            else {
                System.arraycopy(event.values, 0, this.rotationVector, 0, 3);
                has_rotationVector = true;
            }

            SensorManager.getRotationMatrixFromVector(tempMatrix, rotationVector);

            if( !has_original_rotation_matrix ) {
                System.arraycopy(tempMatrix, 0, originalRotationMatrix, 0, 9);
                has_original_rotation_matrix = event.values[3] != 1.0;
            }

            // current = originalT.new
            for(int i=0;i<3;i++) {
                for(int j=0;j<3;j++) {
                    float value = 0.0f;
                    // currentRotationMatrix[ij] = originalRotationMatrix[ki] * tempMatrix[kj]
                    for(int k=0;k<3;k++) {
                        value += getMatrixComponent(originalRotationMatrix, k, i) * getMatrixComponent(tempMatrix, k, j);
                    }
                    setMatrixComponent(currentRotationMatrix, i, j, value);
                }
            }

            if( MyDebug.LOG ) {
                Log.d(TAG, "### values: " + event.values[0] + " , " + event.values[1] + " , " + event.values[2] + " , " + event.values[3]);
                Log.d(TAG, "    " + currentRotationMatrix[0] + " , " + currentRotationMatrix[1] + " , " + currentRotationMatrix[2]);
                Log.d(TAG, "    " + currentRotationMatrix[3] + " , " + currentRotationMatrix[4] + " , " + currentRotationMatrix[5]);
                Log.d(TAG, "    " + currentRotationMatrix[6] + " , " + currentRotationMatrix[7] + " , " + currentRotationMatrix[8]);
            }
        }

        if( hasTarget ) {
            int n_too_far = 0;
            targetAchieved = false;
            for(int indx=0;indx<targetVectors.size();indx++) {
                float [] targetVector = targetVectors.get(indx);
                // first check if we are still "upright"
                // Falk 2026-09-25: geprüft wird jetzt die Rolllage gegen die Erdvertikale und die
                // Neigung gegen die Neigung beim Start der Aufnahme. Upstream verglich dagegen die
                // Hoch-Achse des Handys mit der beim Start - das ist drehwinkelabhängig und erzwang
                // damit ein gerades Panorama: wer schräg beginnt, muss die Neigung aufgeben, weil
                // die Hoch-Achse beim Schwenk um die Erdvertikale zwangsläufig aus der Startlage
                // wandert (bei 90 Grad Schwenk und 15 Grad Neigung sind das 21 Grad, weit über die
                // 3 Grad Toleranz). Für den Stitcher zählt genau umgekehrt: die Aufnahmen müssen
                // durch eine Drehung um die Erdvertikale auseinander hervorgehen, also Rolle gerade
                // und Neigung konstant.
                is_upright = 0;
                pitch_ok = true;
                getUpVector(upVector);
                float sin_tol = (float)Math.sin(this.uprightAngleTol);

                // (a) Rolllage. Waagerecht muss die Rechts-Achse des Handys waagerecht bleiben.
                // Senkrecht (Handy quer) läuft die Schwenkachse entlang der Hoch-Achse (y) des
                // Handys - die bleibt beim Kippen fest, während die Rechts-Achse absichtlich kippt.
                // Geprüft wird deshalb die Hoch-Achse gegen ihren Startanteil an der Erdvertikalen:
                // erst Rollen um die Blickachse dreht sie aus der Startlage heraus.
                if( panorama_vertical ) {
                    setVector(inVector, 0.0f, 1.0f, 0.0f); // vector pointing in "up" direction (device top)
                    transformVector(tempVector, currentRotationMatrix, inVector);
                    float sin_roll = tempVector[0]*upVector[0] + tempVector[1]*upVector[1] + tempVector[2]*upVector[2];
                    float sin_roll_start = upVector[1]; // dot((0,1,0), up) at start (identity)
                    if( Math.abs(sin_roll - sin_roll_start) > sin_tol ) {
                        is_upright = (sin_roll - sin_roll_start < 0) ? 1 : -1;
                    }
                }
                else {
                    setVector(inVector, 1.0f, 0.0f, 0.0f); // vector pointing in "right" direction
                    transformVector(tempVector, currentRotationMatrix, inVector);
                    float sin_roll = tempVector[0]*upVector[0] + tempVector[1]*upVector[1] + tempVector[2]*upVector[2];
                    if( Math.abs(sin_roll) > sin_tol ) {
                        is_upright = (sin_roll < 0) ? 1 : -1;
                    }
                }

                // (b) Neigung: unverändert gegenüber dem Start der Aufnahme. Beim senkrechten
                // Schwenk ändert sich die Neigung (Pitch) absichtlich - dort muss stattdessen die
                // waagerechte Blickrichtung (Gieren) konstant bleiben, sonst laufen die Scheiben
                // schief aufeinander.
                setVector(inVector, 0.0f, 0.0f, -1.0f); // vector pointing behind the device's screen
                transformVector(tempVector, currentRotationMatrix, inVector);
                getStartVector(startVectorTmp);
                if( panorama_vertical ) {
                    float view_up = tempVector[0]*upVector[0] + tempVector[1]*upVector[1] + tempVector[2]*upVector[2];
                    float start_up = startVectorTmp[0]*upVector[0] + startVectorTmp[1]*upVector[1] + startVectorTmp[2]*upVector[2];
                    // horizontaler Anteil der Blickrichtung (senkrecht zur Erdvertikale)
                    float hx = tempVector[0] - view_up*upVector[0];
                    float hy = tempVector[1] - view_up*upVector[1];
                    float hz = tempVector[2] - view_up*upVector[2];
                    float sx = startVectorTmp[0] - start_up*upVector[0];
                    float sy = startVectorTmp[1] - start_up*upVector[1];
                    float sz = startVectorTmp[2] - start_up*upVector[2];
                    float h_mag = (float)Math.sqrt(hx*hx + hy*hy + hz*hz);
                    float s_mag = (float)Math.sqrt(sx*sx + sy*sy + sz*sz);
                    if( h_mag > 1.0e-5f && s_mag > 1.0e-5f ) {
                        float cos_yaw = (hx*sx + hy*sy + hz*sz) / (h_mag*s_mag);
                        if( cos_yaw < (float)Math.cos(this.uprightAngleTol) ) {
                            pitch_ok = false;
                        }
                    }
                }
                else {
                    float sin_pitch = tempVector[0]*upVector[0] + tempVector[1]*upVector[1] + tempVector[2]*upVector[2];
                    float sin_pitch_start = startVectorTmp[0]*upVector[0] + startVectorTmp[1]*upVector[1] + startVectorTmp[2]*upVector[2];
                    if( Math.abs(sin_pitch - sin_pitch_start) > sin_tol ) {
                        pitch_ok = false;
                    }
                }

                float cos_angle = tempVector[0] * targetVector[0] + tempVector[1] * targetVector[1] + tempVector[2] * targetVector[2];
                float angle = (float)Math.acos(cos_angle);
                if( is_upright == 0 && pitch_ok ) {
                    /*if( MyDebug.LOG )
                        Log.d(TAG, "gyro vector angle with target: " + Math.toDegrees(angle) + " degrees");*/
                    if( angle <= targetAngle ) {
                        if( MyDebug.LOG )
                            Log.d(TAG, "    ### achieved target angle: " + Math.toDegrees(angle) + " degrees");
                        targetAchieved = true;
                        if( targetCallback != null ) {
                            //targetCallback.onAchieved(indx);
                            if( has_lastTargetAngle ) {
                                if( MyDebug.LOG )
                                    Log.d(TAG, "        last target angle: " + Math.toDegrees(lastTargetAngle) + " degrees");
                                if( angle > lastTargetAngle ) {
                                    // started to get worse, so call callback
                                    targetCallback.onAchieved(indx);
                                }
                                // else, don't call callback yet, as we may get closer to the target
                            }
                        }
                        // only bother setting the lastTargetAngle if within the target angle - otherwise we'll have problems if there is more than one target set
                        has_lastTargetAngle = true;
                        lastTargetAngle = angle;
                    }
                }

                if( angle > tooFarAngle ) {
                    n_too_far++;
                }
            /*if( MyDebug.LOG )
                Log.d(TAG, "targetAchieved? " + targetAchieved);*/
            }
            if( n_too_far > 0 && n_too_far == targetVectors.size() ) {
                if( targetCallback != null ) {
                    targetCallback.onTooFar();
                }
            }
        }
    }

    /*  This returns a 3D vector, that represents the current direction that the device is pointing (looking towards the screen),
     *  relative to when startRecording() was called.
     *  That is, the coordinate system is defined by the device's initial orientation when startRecording() was called:
     *      X: -ve to +ve is left to right
     *      Y: -ve to +ve is down to up
     *      Z: -ve to +ve is out of the screen to behind the screen
     *  So if the device hasn't changed orientation, this will return (0, 0, -1).
     *  (1, 0, 0) means the device has rotated 90 degrees so it's now pointing to the right.
     * @param result An array of length 3 to store the returned vector.
     */
    /*void getRelativeVector(float [] result) {
        setVector(inVector, 0.0f, 0.0f, -1.0f); // vector pointing behind the device's screen
        transformVector(result, currentRotationMatrix, inVector);
    }*/

    /*void getRelativeInverseVector(float [] result) {
        setVector(inVector, 0.0f, 0.0f, -1.0f); // vector pointing behind the device's screen
        transformTransposeVector(result, currentRotationMatrix, inVector);
    }*/

    public void getRelativeInverseVector(float [] out, float [] in) {
        transformTransposeVector(out, currentRotationMatrix, in);
    }

    public void getRelativeInverseVectorGyroOnly(float [] out, float [] in) {
        transformTransposeVector(out, currentRotationMatrixGyroOnly, in);
    }

    public void getRotationMatrix(float [] out) {
        System.arraycopy(currentRotationMatrix, 0, out, 0, 9);
    }

    // for testing

    public void testForceTargetAchieved(int indx) {
        if( MyDebug.LOG )
            Log.d(TAG, "testForceTargetAchieved: " + indx);
        if( targetCallback != null ) {
            targetCallback.onAchieved(indx);
        }
    }
}

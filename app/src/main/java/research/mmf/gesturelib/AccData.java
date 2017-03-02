package research.mmf.gesturelib;

/***
 * data structure to hold the acdelerometer data
 * Author: Mingming Fan
 * Contact: fmmbupt@gmail.com
 * Contact the author for use of the code
 * */

public class AccData
{
	private float x;
	private float y;
	private float z;

    public AccData(float xx, float yy, float zz)
	{
		this.x = xx;
		this.y = yy;
		this.z = zz;
	}

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public String asString() {
        return x + " " + y + " " + z;
    }
}
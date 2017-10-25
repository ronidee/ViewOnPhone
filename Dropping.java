package ronidea.viewonphone;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class Dropping {

    final static int TYPE_NULL = 0;
    final static int TYPE_URL  = 1;
    final static int TYPE_IMG  = 2;
    final static int TYPE_EMAIL  = 3;


    /*      VALUES      */

    private int    type = 0;
    private String url  = "";
    private String name = "tollesbild.jpg";
    private String pw   = "wrong";
    private Bitmap img  = null;



    /*      SETTER      */

    void setType(int type)      {
        this.type = type;
    }
    void setUrl(String url)   {
        this.url = url;
    }
    void setImg (String base64encodedImgArray) {
        byte[] imgArray = Base64.decode(base64encodedImgArray, Base64.DEFAULT);
        img = BitmapFactory.decodeByteArray(imgArray , 0, imgArray.length);
    }
    void setName(String name)   {
        this.name = name;
    }
    void setPw  (String pw)     {
        this.pw = pw;
    }



    /*      GETTER      */

    int getType() {
        return type;
    }
    String getURL() {
        return url;
    }
    Bitmap getImage() {
        return img;
    }
    String getName() {
        return name;
    }
    String getPw() {
        return pw;
    }
}

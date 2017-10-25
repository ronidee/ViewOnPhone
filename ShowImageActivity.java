package ronidea.viewonphone;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;


/**
 * Created by roni on 16.10.17.
 */

public class ShowImageActivity extends Activity {
    ImageView iv_singleImage;
    static Bitmap bmp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image);


        iv_singleImage = findViewById(R.id.iv_singleImage);
        iv_singleImage.setImageBitmap(bmp);
    }
}

package es.rcti.demoprinterplus.pruebabd; // Ajusta esto a tu paquete

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class SignatureView extends View {
    private ArrayList<Path> paths;
    private Path currentPath;
    private Paint paint;
    private boolean hasSignature;
    private OnSignatureListener signatureListener;

    public interface OnSignatureListener {
        void onSignatureDrawn(boolean hasSignature);
    }

    public SignatureView(Context context) {
        super(context);
        init();
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paths = new ArrayList<>();
        currentPath = new Path();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    public void setOnSignatureListener(OnSignatureListener listener) {
        this.signatureListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Dibujar todos los paths guardados
        for (Path path : paths) {
            canvas.drawPath(path, paint);
        }
        // Dibujar el path actual
        canvas.drawPath(currentPath, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                hasSignature = true;
                if (signatureListener != null) {
                    signatureListener.onSignatureDrawn(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                paths.add(currentPath);
                currentPath = new Path();
                break;
        }
        invalidate();
        return true;
    }

    public void clear() {
        paths.clear();
        currentPath = new Path();
        hasSignature = false;
        if (signatureListener != null) {
            signatureListener.onSignatureDrawn(false);
        }
        invalidate();
    }

    public Bitmap getBitmap() {
        if (!hasSignature) return null;

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        for (Path path : paths) {
            canvas.drawPath(path, paint);
        }
        canvas.drawPath(currentPath, paint);

        return bitmap;
    }
}
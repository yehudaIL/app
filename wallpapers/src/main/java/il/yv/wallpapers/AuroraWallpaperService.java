package il.yv.wallpapers;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import java.util.Random;

public class AuroraWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){ return new AuroraEngine(); }

    private class AuroraEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final Random random = new Random(77);
        private final float[] px = new float[95], py = new float[95], speed = new float[95], size = new float[95];
        private boolean visible = false;
        private long start = System.currentTimeMillis();
        private int style = 0;
        private final Runnable draw = new Runnable(){ @Override public void run(){ drawFrame(); } };

        AuroraEngine(){
            for(int i=0;i<px.length;i++){
                px[i]=random.nextFloat(); py[i]=random.nextFloat();
                speed[i]=.20f+random.nextFloat()*.85f; size[i]=1f+random.nextFloat()*4f;
            }
        }
        @Override public void onVisibilityChanged(boolean v){ visible=v; if(v) drawFrame(); else handler.removeCallbacks(draw); }
        @Override public void onSurfaceChanged(SurfaceHolder h,int f,int w,int he){ super.onSurfaceChanged(h,f,w,he); drawFrame(); }
        @Override public void onSurfaceDestroyed(SurfaceHolder h){ visible=false; handler.removeCallbacks(draw); super.onSurfaceDestroyed(h); }
        @Override public void onDestroy(){ visible=false; handler.removeCallbacks(draw); super.onDestroy(); }

        private void drawFrame(){
            Canvas c=null;
            try { c=getSurfaceHolder().lockCanvas(); if(c!=null) render(c); }
            catch(Exception ignored){} finally { if(c!=null) getSurfaceHolder().unlockCanvasAndPost(c); }
            handler.removeCallbacks(draw);
            if(visible) handler.postDelayed(draw,33);
        }

        private void render(Canvas c){
            float w=c.getWidth(), h=c.getHeight();
            float t=(System.currentTimeMillis()-start)/1000f;
            int a=0xFF050817, b=0xFF071B35, neon=0xFF00E5FF, hot=0xFFFF2BD6;
            if(style==1){ neon=0xFFFFB000; hot=0xFFFF3158; b=0xFF24102D; }
            if(style==2){ neon=0xFF7CFF3A; hot=0xFF00D5A8; b=0xFF06251F; }

            p.setShader(new LinearGradient(0,0,w,h,a,b,Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p); p.setShader(null);

            // Fast-moving diagonal energy waves
            for(int band=0;band<5;band++){
                float phase=t*(1.25f+band*.19f)+band*1.7f;
                float base=h*(.20f+band*.145f);
                path.reset();
                path.moveTo(-w*.15f,base);
                for(int x=0;x<=12;x++){
                    float xx=w*x/12f;
                    float yy=base+(float)Math.sin(x*.83f+phase)*h*.105f
                        +(float)Math.sin(x*.37f-phase*1.4f)*h*.045f;
                    path.lineTo(xx,yy);
                }
                path.lineTo(w,h); path.lineTo(-w*.15f,h); path.close();
                int color=(band%2==0)?neon:hot;
                p.setShader(new LinearGradient(0,base-h*.12f,0,h,color&0x66FFFFFF,color&0x00FFFFFF,Shader.TileMode.CLAMP));
                p.setAlpha(115); c.drawPath(path,p); p.setShader(null); p.setAlpha(255);

                // bright racing edge
                path.reset();
                for(int x=0;x<=12;x++){
                    float xx=w*x/12f;
                    float yy=base+(float)Math.sin(x*.83f+phase)*h*.105f
                        +(float)Math.sin(x*.37f-phase*1.4f)*h*.045f;
                    if(x==0) path.moveTo(xx,yy); else path.lineTo(xx,yy);
                }
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3.5f+band*.7f);
                p.setColor(color); p.setAlpha(180); c.drawPath(path,p);
                p.setStyle(Paint.Style.FILL); p.setAlpha(255);
            }

            // Huge moving plasma core with a bright halo
            float cx=w*(.5f+.30f*(float)Math.sin(t*.82f));
            float cy=h*(.48f+.23f*(float)Math.cos(t*1.05f));
            float pulse=1f+.20f*(float)Math.sin(t*5.5f);
            float radius=Math.min(w,h)*.19f*pulse;
            p.setShader(new RadialGradient(cx,cy,radius*2.4f,
                new int[]{neon,neon&0x88FFFFFF,hot&0x44FFFFFF,Color.TRANSPARENT},
                new float[]{0f,.28f,.62f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,radius*2.4f,p); p.setShader(null);
            p.setColor(Color.WHITE); p.setAlpha(225); c.drawCircle(cx,cy,radius*.12f,p); p.setAlpha(255);

            // Fast streaking particles, wrapping around screen
            for(int i=0;i<px.length;i++){
                float x=((px[i]+t*speed[i]*.20f)%1.15f)*w-w*.07f;
                float y=(py[i]+(float)Math.sin(t*2.4f+i)*.035f)*h;
                float len=size[i]*(8f+speed[i]*24f);
                p.setColor(i%3==0?hot:neon); p.setAlpha(90+(int)(130*Math.abs(Math.sin(t*4+i))));
                p.setStrokeWidth(size[i]); c.drawLine(x,y,x-len,y-len*.18f,p);
                c.drawCircle(x,y,size[i]*.8f,p);
            }
            p.setAlpha(255);
        }
    }
}

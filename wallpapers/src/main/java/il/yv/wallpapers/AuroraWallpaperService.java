package il.yv.wallpapers;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.Random;

public class AuroraWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){return new AuroraEngine();}
    private class AuroraEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path=new Path();
        private final Random random=new Random(18);
        private final float[] sx=new float[48], sy=new float[48], sr=new float[48];
        private boolean visible=false;
        private long start=System.currentTimeMillis();
        private int width=1,height=1;
        private final Runnable draw=new Runnable(){@Override public void run(){drawFrame();}};
        AuroraEngine(){for(int i=0;i<sx.length;i++){sx[i]=random.nextFloat();sy[i]=random.nextFloat();sr[i]=.5f+random.nextFloat()*1.5f;}}
        @Override public void onVisibilityChanged(boolean v){visible=v;if(v)drawFrame();else handler.removeCallbacks(draw);}
        @Override public void onSurfaceChanged(SurfaceHolder h,int format,int w,int he){super.onSurfaceChanged(h,format,w,he);width=w;height=he;drawFrame();}
        @Override public void onSurfaceDestroyed(SurfaceHolder h){visible=false;handler.removeCallbacks(draw);super.onSurfaceDestroyed(h);}
        @Override public void onDestroy(){visible=false;handler.removeCallbacks(draw);super.onDestroy();}
        private void drawFrame(){
            SurfaceHolder holder=getSurfaceHolder(); Canvas c=null;
            try{
                c=holder.lockCanvas(); if(c!=null)render(c);
            }catch(Exception ignored){}finally{if(c!=null)holder.unlockCanvasAndPost(c);}
            handler.removeCallbacks(draw); if(visible)handler.postDelayed(draw,42);
        }
        private void render(Canvas c){
            float w=c.getWidth(),h=c.getHeight(),t=(System.currentTimeMillis()-start)/1000f;
            SharedPreferences pref=getSharedPreferences("luma",MODE_PRIVATE); int style=pref.getInt("style",0);
            int top=style==0?0xFF0B1530:style==1?0xFF170E2B:0xFF041B35;
            int mid=style==0?0xFF443C91:style==1?0xFF78429B:0xFF08708F;
            int low=style==0?0xFF123D4B:style==1?0xFF542D68:0xFF07516B;
            paint.setShader(new LinearGradient(0,0,w,h,top,low,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,paint);paint.setShader(null);
            paint.setShader(new LinearGradient(0,h*.2f,w,h*.85f,new int[]{0x00101030,mid,low,0x00101030},null,Shader.TileMode.CLAMP));
            for(int band=0;band<3;band++){
                path.reset();float phase=t*(.20f+band*.045f)+band*2.1f;
                path.moveTo(0,h*(.38f+band*.12f));
                for(int x=0;x<=8;x++){float xx=w*x/8f;float yy=h*(.39f+band*.115f)+(float)Math.sin(x*.72+phase)*h*.095f+(float)Math.sin(x*.31-phase*.7)*h*.055f;path.lineTo(xx,yy);}
                path.lineTo(w,h);path.lineTo(0,h);path.close();
                paint.setAlpha(95-band*15);c.drawPath(path,paint);
            }
            paint.setShader(null);paint.setAlpha(255);
            paint.setColor(0xAAFFFFFF);
            for(int i=0;i<sx.length;i++){
                float tw=.35f+.65f*(float)Math.abs(Math.sin(t*.8f+i));
                paint.setAlpha((int)(70+150*tw));c.drawCircle(sx[i]*w,sy[i]*h,sr[i]*tw,paint);
            }
            paint.setAlpha(255);
            paint.setShader(new LinearGradient(0,h*.5f,w,h,0x0036E6D1,0x4436E6D1,Shader.TileMode.CLAMP));
            c.drawCircle(w*.76f,h*.22f,Math.min(w,h)*.12f,paint);paint.setShader(null);
        }
    }
}

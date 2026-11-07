package il.yv.wallpapers;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(8,13,28), panel=Color.rgb(19,27,52), white=Color.WHITE, muted=0xFFABB5D2, violet=0xFF9585FF, mint=0xFF42DCCB;
    private SharedPreferences prefs;
    private TextView[] choices=new TextView[3];
    private int selected=0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        prefs=getSharedPreferences("luma",MODE_PRIVATE);
        selected=prefs.getInt("style",0);
        build();
    }
    private GradientDrawable shape(int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private TextView text(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); return t;
    }
    private void build() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(22,22,22,18); root.setBackgroundColor(bg); setContentView(root);
        TextView brand=text("✦  LUMA",15,mint,true); brand.setLetterSpacing(.16f); root.addView(brand);
        TextView title=text("המסך שלך,\nבצבעים חיים.",31,white,true); title.setLineSpacing(0,1.05f);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2); tp.topMargin=20; root.addView(title,tp);
        TextView sub=text("טפטים מונפשים שהופכים כל פתיחה של הטלפון לרגע יפה.",14,muted,false);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2); sp.topMargin=9; root.addView(sub,sp);

        Preview preview=new Preview(this); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,0,1); pp.topMargin=22; pp.bottomMargin=18; root.addView(preview,pp);
        TextView label=text("בחר אווירה",17,white,true); root.addView(label);
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,58); rp.topMargin=12; rp.bottomMargin=18; root.addView(row,rp);
        String[] names={"זוהר צפוני","חלום סגול","אוקיינוס"};
        for(int i=0;i<3;i++) {
            final int index=i; TextView c=text(names[i],12,white,true); c.setGravity(Gravity.CENTER);
            c.setBackground(shape(i==selected?0xFF5549A8:panel,18));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1); cp.setMargins(4,0,4,0); row.addView(c,cp); choices[i]=c;
            c.setOnClickListener(v->{selected=index;prefs.edit().putInt("style",index).apply();refreshChoices();preview.invalidate();});
        }
        Button apply=new Button(this); apply.setText("הגדר את הטפט שלי  ✦"); apply.setTextColor(Color.WHITE); apply.setTextSize(16); apply.setAllCaps(false); apply.setTypeface(Typeface.DEFAULT,Typeface.BOLD); apply.setBackground(shape(0xFF6658D8,24));
        root.addView(apply,new LinearLayout.LayoutParams(-1,58));
        apply.setOnClickListener(v->openWallpaperPicker());
        TextView foot=text("אנימציה עדינה  •  מותאם למסך שלך  •  ללא אינטרנט",11,0xFF7F8AA8,false); foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2); fp.topMargin=14; root.addView(foot,fp);
    }
    private void refreshChoices(){for(int i=0;i<3;i++)choices[i].setBackground(shape(i==selected?0xFF5549A8:panel,18));}
    private void openWallpaperPicker(){
        try {
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,AuroraWallpaperService.class));
            startActivity(i);
        } catch(Exception e) {
            try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch(Exception ignored){Toast.makeText(this,"לא ניתן לפתוח את בחירת הטפט במכשיר הזה",Toast.LENGTH_LONG).show();}
        }
    }
    private class Preview extends View {
        private final android.graphics.Paint p=new android.graphics.Paint(3);
        Preview(android.content.Context c){super(c);}
        @Override protected void onDraw(android.graphics.Canvas c){
            super.onDraw(c); float w=getWidth(),h=getHeight();
            android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                selected==0?new int[]{0xFF111B3B,0xFF6655B8,0xFF35BFAF}:selected==1?new int[]{0xFF17112F,0xFF9B58B8,0xFFF09BC7}:new int[]{0xFF061D3A,0xFF087C9A,0xFF54D7C2});
            g.setCornerRadius(30); g.setBounds(0,0,getWidth(),getHeight()); g.draw(c);
            p.setColor(0x66FFFFFF); p.setStyle(android.graphics.Paint.Style.STROKE); p.setStrokeWidth(1.5f);
            c.drawCircle(w*.72f,h*.28f,h*.12f,p); c.drawCircle(w*.72f,h*.28f,h*.18f,p); p.setStyle(android.graphics.Paint.Style.FILL);
            p.setColor(0xCCFFFFFF); p.setTextSize(14); p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD)); c.drawText("LUMA  •  LIVE WALLPAPER",22,h-28,p);
            p.setColor(0x99FFFFFF); p.setTextSize(11); c.drawText("PREVIEW",22,28,p);
        }
    }
}

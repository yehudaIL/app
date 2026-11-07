package il.yehuda.hotspot;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class HotspotTileService extends TileService {
    @Override public void onTileAdded() {
        super.onTileAdded();
        updateTile();
    }

    @Override public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setLabel(getString(R.string.tile_label));
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override public void onClick() {
        super.onClick();
        Intent intent = new Intent("android.settings.TETHER_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivityAndCollapse(intent);
        } catch (Exception first) {
            Intent fallback = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivityAndCollapse(fallback); }
            catch (Exception ignored) {
                Intent general = new Intent(Settings.ACTION_SETTINGS);
                general.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAndCollapse(general);
            }
        }
    }
}

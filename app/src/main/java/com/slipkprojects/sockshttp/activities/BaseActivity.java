package com.slipkprojects.sockshttp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.slipkprojects.sockshttp.preference.SettingsPreference;
import com.slipkprojects.ultrasshservice.config.Settings;
import android.support.v7.app.AppCompatDelegate;
import android.content.Context;
import com.slipkprojects.sockshttp.preference.LocaleHelper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import static android.content.pm.PackageManager.GET_META_DATA;
import org.json.JSONObject;
import java.io.File;
import com.slipkprojects.sockshttp.util.AESCrypt;
import java.io.InputStream;
import com.slipkprojects.sockshttp.util.Utils;
import java.io.FileInputStream;
/**
 * Created by Pankaj on 03-11-2017.
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;
    public static final String PASSWORD = "hirodevs";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setTheme(ThemeUtil.getThemeId(mTheme));
		
		setModoNoturnoLocal();
			
		resetTitles();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.setLocale(base));
	}
	
	public void setModoNoturnoLocal() {
		boolean is = new Settings(this)
			.getModoNoturno().equals("on");

		int night_mode = is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
		//AppCompatDelegate.setDefaultNightMode(night_mode);
		getDelegate().setLocalNightMode(night_mode);
	}
	protected JSONObject getJSONConfig2(Context context) throws Exception {
		String json = null;
        File file = new File(context.getFilesDir(), "Config.json");
		if (file.exists()) {
			String json_file = Utils.readStream(new FileInputStream(file));
			json = AESCrypt.decrypt(PASSWORD, json_file);
			// return new JSONObject(json);
		} else {
			InputStream inputStream = context.getAssets().open("config/config.json");
			json = AESCrypt.decrypt(PASSWORD, Utils.readStream(inputStream));
			// return new JSONObject(json);
		}
        return new JSONObject(json);
    }
	
	
	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}

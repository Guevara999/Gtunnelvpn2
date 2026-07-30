package com.slipkprojects.sockshttp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.slipkprojects.sockshttp.R;
import com.slipkprojects.sockshttp.SocksHttpApp;
import com.slipkprojects.sockshttp.activities.BaseActivity;
import com.slipkprojects.sockshttp.activities.ConfigGeralActivity;
import com.slipkprojects.sockshttp.adapter.LogsAdapter;
import com.slipkprojects.sockshttp.adapter.SpinnerAdapter;
import com.slipkprojects.sockshttp.util.AESCrypt;
import com.slipkprojects.sockshttp.util.ConfigUpdate;
import com.slipkprojects.sockshttp.util.ConfigUtil;
import com.slipkprojects.sockshttp.util.Utils;
import com.slipkprojects.ultrasshservice.LaunchVpn;
import com.slipkprojects.ultrasshservice.SocksHttpService;
import com.slipkprojects.ultrasshservice.StatisticGraphData;
import com.slipkprojects.ultrasshservice.StatisticGraphData.DataTransferStats;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.tunnel.TunnelManagerHelper;
import com.slipkprojects.ultrasshservice.tunnel.TunnelUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import com.slipkprojects.sockshttp.adapter.PromoAdapter;
import android.content.ClipboardManager;
import android.content.ClipData;
import org.json.JSONArray;
import android.widget.CheckBox;
import android.util.TypedValue;
import android.preference.PreferenceManager;
import com.slipkprojects.sockshttp.activities.InjectorService;
import android.support.design.widget.Snackbar;
import android.view.View.OnClickListener;
import com.slipkprojects.sockshttp.activities.CustomDNS;

/**
 * Activity Principal
 * @author SlipkHunter
 */

public class SocksHttpMainActivity extends BaseActivity
	implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,
				CompoundButton.OnCheckedChangeListener, SkStatus.StateListener
{

    private TextView connectionStatus;

    private LogsAdapter mLogAdapter;

    private RecyclerView logList;

    private ViewPager vp;

    private TabLayout tabs;

	private TextView bytesIn;

	private TextView bytesOut;

	private CountDownTimer countDown;

    @Override
    public void onCheckedChanged(CompoundButton p1, boolean p2) {
    }

	private static final String TAG = SocksHttpMainActivity.class.getSimpleName();
	private static final String UPDATE_VIEWS = "MainUpdate";
	public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";
	private static ArrayList modeList = new ArrayList();
	
	
	private Settings mConfig;
	private Toolbar toolbar_main;
	private Handler mHandler;
	
	private LinearLayout mainLayout;
	private LinearLayout loginLayout;
	private LinearLayout proxyInputLayout;
	private TextView proxyText;
	private RadioGroup metodoConexaoRadio;
	private LinearLayout payloadLayout;
	private TextInputEditText payloadEdit;
	private SwitchCompat customPayloadSwitch;
	private Button starterButton;
	
	private ImageButton inputPwShowPass;
	private TextInputEditText inputPwUser;
	private TextInputEditText inputPwPass;
	
	private LinearLayout configMsgLayout;
	private TextView configMsgText;

	private AdView adsBannerView;

	private ConfigUtil config;
	public static int PICK_FILE = 1;
	private Spinner serverSpinner;
	private Spinner payloadSpinner;
    private static final String[] tabTitle = {"Home","Log"};
	private SpinnerAdapter serverAdapter;
	private PromoAdapter payloadAdapter;
	private ArrayList<JSONObject> serverList;
	private ArrayList<JSONObject> payloadList;
	private CheckBox startSSHCheckbox;
	private CheckBox dnsCheckBox;

	@Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		mHandler = new Handler();
		mConfig = new Settings(this);
		SharedPreferences prefs = getSharedPreferences(SocksHttpApp.PREFS_GERAL, Context.MODE_PRIVATE);
		boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
		int lastVersion = prefs.getInt("last_version", 0);
		if (showFirstTime)
        {
            SharedPreferences.Editor pEdit = prefs.edit();
            pEdit.putBoolean("connect_first_time", false);
            pEdit.apply();
			Settings.setDefaultConfig(this);
			showBoasVindas();
        }

		try {
			int idAtual = ConfigParser.getBuildId(this);

			if (lastVersion < idAtual) {
				SharedPreferences.Editor pEdit = prefs.edit();
				pEdit.putInt("last_version", idAtual);
				pEdit.apply();

				if (!showFirstTime) {
					if (lastVersion <= 12) {
						Settings.setDefaultConfig(this);
						Settings.clearSettings(this);

						Toast.makeText(this, "As configurações foram limpas para evitar bugs",
							Toast.LENGTH_LONG).show();
					}
				}

			}
		} catch(IOException e) {}
		doLayout();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_VIEWS);
		filter.addAction(OPEN_LOGS);
		
		LocalBroadcastManager.getInstance(this)
			.registerReceiver(mActivityReceiver, filter);
			
		doUpdateLayout();
	}


	/**
	 * Layout
	 */
	 
	private void doLayout() {
		final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_main_drawer);
		toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar_main);
		adsBannerView = (AdView) findViewById(R.id.adBannerMainView);
		if (TunnelUtils.isNetworkOnline(SocksHttpMainActivity.this)) {
			adsBannerView.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					if (adsBannerView != null) {
						adsBannerView.setVisibility(View.VISIBLE);
					}
				}
			});
			adsBannerView.loadAd(new AdRequest.Builder()
				.build());
		}
		startSSHCheckbox = (CheckBox) findViewById(R.id.startSSHCheckbox);
		startSSHCheckbox.setText("Start SSH");
		startSSHCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
		startSSHCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPref.edit().putBoolean("startSSHCheck",isChecked).apply();
				}
			});
		dnsCheckBox=(CheckBox) findViewById(R.id.useDns);
		dnsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPref.edit().putBoolean("startDns",isChecked).apply();
				}
			});
		dnsCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
		mainLayout = (LinearLayout) findViewById(R.id.activity_mainLinearLayout);
		loginLayout = (LinearLayout) findViewById(R.id.activity_mainInputPasswordLayout);
		starterButton = (Button) findViewById(R.id.activity_starterButtonMain);
		inputPwUser = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordUserEdit);
		inputPwPass = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordPassEdit);
		inputPwShowPass = (ImageButton) findViewById(R.id.activity_mainInputShowPassImageButton);

		((TextView) findViewById(R.id.activity_mainAutorText))
			.setOnClickListener(this);

		proxyInputLayout = (LinearLayout) findViewById(R.id.activity_mainInputProxyLayout);
		proxyText = (TextView) findViewById(R.id.activity_mainProxyText);
      
        final SharedPreferences prefs = mConfig.getPrefsPrivate();
        SharedPreferences.Editor edit = prefs.edit();
		SharedPreferences sPrefs = mConfig.getPrefsPrivate();
        sPrefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
		sPrefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
        config = new ConfigUtil(this);
		serverSpinner = (Spinner) findViewById(R.id.serverSpinner);
		payloadSpinner = (Spinner) findViewById(R.id.payloadSpinner);
        connectionStatus = (TextView) findViewById(R.id.connection_status);
		serverList = new ArrayList<>();
		payloadList = new ArrayList<>();

		serverAdapter = new SpinnerAdapter(this, R.id.serverSpinner, serverList);
		payloadAdapter = new PromoAdapter(this, R.id.payloadSpinner, payloadList);

		serverSpinner.setAdapter(serverAdapter);
		payloadSpinner.setAdapter(payloadAdapter);

		loadServer();
		loadNetworks();
		updateConfig(true);

		
		
		metodoConexaoRadio = (RadioGroup) findViewById(R.id.activity_mainMetodoConexaoRadio);
		customPayloadSwitch = (SwitchCompat) findViewById(R.id.activity_mainCustomPayloadSwitch);

		starterButton.setOnClickListener(this);
		proxyInputLayout.setOnClickListener(this);
		dnsCheckBox.setOnClickListener(this);

		payloadLayout = (LinearLayout) findViewById(R.id.activity_mainInputPayloadLinearLayout);
		payloadEdit = (TextInputEditText) findViewById(R.id.activity_mainInputPayloadEditText);
		bytesIn = (TextView) findViewById(R.id.bytesIn);
        bytesOut = (TextView) findViewById(R.id.bytesOut);
		configMsgLayout = (LinearLayout) findViewById(R.id.activity_mainMensagemConfigLinearLayout);
		configMsgText = (TextView) findViewById(R.id.activity_mainMensagemConfigTextView);

		// fix bugs
		if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));
			}
		}
		else {
			payloadEdit.setText(mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY));
		}
        customPayloadSwitch.setChecked(true);
        
        edit.putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, !true);
        setPayloadSwitch(prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT), true);
		metodoConexaoRadio.setOnCheckedChangeListener(this);
		inputPwShowPass.setOnClickListener(this);
        
        doTabs();
	}
	
	private void updateHeaderCallback() {
		DataTransferStats dataTransferStats = StatisticGraphData.getStatisticData().getDataTransferStats();
		bytesIn.setText(dataTransferStats.byteCountToDisplaySize(dataTransferStats.getTotalBytesReceived(), false));
		bytesOut.setText(dataTransferStats.byteCountToDisplaySize(dataTransferStats.getTotalBytesSent(), false));
	}
    
    public void doTabs() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLogAdapter = new LogsAdapter(layoutManager,this);
        logList = (RecyclerView) findViewById(R.id.recyclerLog);
        logList.setAdapter(mLogAdapter);
        logList.setLayoutManager(layoutManager);
        mLogAdapter.scrollToLastPosition();
        vp = (ViewPager)findViewById(R.id.viewpager);
        tabs = (TabLayout)findViewById(R.id.tablayout);
        vp.setAdapter(new MyAdapter(Arrays.asList(tabTitle)));
        vp.setOffscreenPageLimit(2);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(vp);
		
		vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
            {
                @Override
                public void onPageSelected(int position)
                {
                    if (position == 0) {
                        toolbar_main.getMenu().clear();
						getMenuInflater().inflate(R.menu.main_menu, toolbar_main.getMenu());
                    } else if (position == 1) {
                        toolbar_main.getMenu().clear();
						getMenuInflater().inflate(R.menu.logs_menu, toolbar_main.getMenu());
                    }
                }
			});
        }
        
    public class MyAdapter extends PagerAdapter
    {

        @Override
        public int getCount()
        {
            // TODO: Implement this method
            return 2;
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2)
        {
            // TODO: Implement this method
            return p1 == p2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            int[] ids = new int[]{R.id.tab1, R.id.tab2};
            int id = 0;
            id = ids[position];
            // TODO: Implement this method
            return findViewById(id);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            // TODO: Implement this method
            return titles.get(position);
        }

        private List<String> titles;
        public MyAdapter(List<String> str)
        {
            titles = str;
        }
	}
        
	private void doUpdateLayout() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isRunning = SkStatus.isTunnelActive();
		int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
		
		setStarterButton(starterButton, this);
		setPayloadSwitch(tunnelType, !prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true));
		startSSHCheckbox.setEnabled(!isRunning);
		startSSHCheckbox.setChecked(mPref.getBoolean("startSSHCheck",true));
		dnsCheckBox.setChecked(mPref.getBoolean("startDns",true));
		dnsCheckBox.setEnabled(!isRunning);
		String proxyStr = getText(R.string.no_value).toString();

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			proxyStr = "*******";
			proxyInputLayout.setEnabled(false);
		}
		else {
			String proxy = mConfig.getPrivString(Settings.PROXY_IP_KEY);

			if (proxy != null && !proxy.isEmpty())
				proxyStr = String.format("%s:%s", proxy, mConfig.getPrivString(Settings.PROXY_PORTA_KEY));
			proxyInputLayout.setEnabled(!isRunning);
		} 

		proxyText.setText(proxyStr);


		switch (tunnelType) {
			case Settings.bTUNNEL_TYPE_SSH_DIRECT:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHDirectRadioButton))
					.setChecked(true);
				customPayloadSwitch.setChecked(true);
				break;

			case Settings.bTUNNEL_TYPE_SSH_PROXY:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHProxyRadioButton))
					.setChecked(true);
				customPayloadSwitch.setChecked(true);
                break;
            case Settings.bTUNNEL_TYPE_SSH_SSL:
                ((AppCompatRadioButton) findViewById(R.id.activity_mainSSHSSLRadioButton))
                    .setChecked(true);
				customPayloadSwitch.setChecked(false);
                break;
			case Settings.bTUNNEL_TYPE_PAY_SSL:
				customPayloadSwitch.setChecked(true);
                break;
			case Settings.bTUNNEL_TYPE_SSL_RP:
				customPayloadSwitch.setChecked(true);
                break;
		}

		int msgVisibility = View.GONE;
		int loginVisibility = View.GONE;
		String msgText = "";
		boolean enabled_radio = !isRunning;

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			
			if (prefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				loginVisibility = View.VISIBLE;
				
				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));
				
				inputPwUser.setEnabled(!isRunning);
				inputPwPass.setEnabled(!isRunning);
				inputPwShowPass.setEnabled(!isRunning);
				
				//inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}
			
			String msg = mConfig.getPrivString(Settings.CONFIG_MENSAGEM_KEY);
			if (!msg.isEmpty()) {
				msgText = msg.replace("\n", "<br/>");
				msgVisibility = View.VISIBLE;
			}
			
			if (mConfig.getPrivString(Settings.PROXY_IP_KEY).isEmpty() ||
					mConfig.getPrivString(Settings.PROXY_PORTA_KEY).isEmpty()) {
				enabled_radio = false;
			}
		}

		loginLayout.setVisibility(loginVisibility);
		configMsgText.setText(msgText.isEmpty() ? "" : Html.fromHtml(msgText));
		configMsgLayout.setVisibility(msgVisibility);
		
		// desativa/ativa radio group
		for (int i = 0; i < metodoConexaoRadio.getChildCount(); i++) {
			metodoConexaoRadio.getChildAt(i).setEnabled(enabled_radio);
		}
	}
	
	
	private synchronized void doSaveData() {
        try {
            SharedPreferences prefs = mConfig.getPrefsPrivate();
            SharedPreferences.Editor edit = prefs.edit();
			if (!prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
				if (!prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
					int pos = payloadSpinner.getSelectedItemPosition();
					// int modeType = prefs.getInt("TunnelMode",modeGroup.getCheckedRadioButtonId());


					boolean directModeType = config.getNetworksArray().getJSONObject(pos).getBoolean("isSSL");
					boolean sshssltype =  config.getNetworksArray().getJSONObject(pos).getBoolean("wsPayload");
					boolean remotessltype = config.getNetworksArray().getJSONObject(pos).getBoolean("remoteHost");
					boolean slowdnstype = config.getNetworksArray().getJSONObject(pos).getBoolean("SlowDns");
					if (directModeType) {
						prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_SSL).apply();
						String sni = config.getNetworksArray().getJSONObject(pos).getString("SNI");
						edit.putString(Settings.CUSTOM_SNI, sni);
						edit.apply();

					} else if (sshssltype) {
						prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_PAY_SSL).apply();
						String payload = config.getNetworksArray().getJSONObject(pos).getString("Payload");
						String snissl = config.getNetworksArray().getJSONObject(pos).getString("SNI");
						edit.putString(Settings.CUSTOM_PAYLOAD_KEY, payload);
						edit.putString(Settings.CUSTOM_SNI, snissl);
						edit.apply();
					}else if (remotessltype){
						prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSL_RP).apply();
						String payloadrp = config.getNetworksArray().getJSONObject(pos).getString("Payload");
						String sslrp = config.getNetworksArray().getJSONObject(pos).getString("SNI");
						edit.putString(Settings.CUSTOM_PAYLOAD_KEY, payloadrp);
						edit.putString(Settings.CUSTOM_SNI, sslrp);
						edit.apply();
					}else if (slowdnstype){
						prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SLOWDNS).apply();
						String chvKey = config.getNetworksArray().getJSONObject(pos).getString("chaveKey");
						String nvKey = config.getNetworksArray().getJSONObject(pos).getString("serverNameKey");
						String dnsKey = config.getNetworksArray().getJSONObject(pos).getString("dnsKey");

						edit.putString(Settings.CHAVE_KEY, chvKey);
						edit.putString(Settings.NAMESERVER_KEY, nvKey);
						edit.putString(Settings.DNS_KEY, dnsKey);
						edit.apply();
					} else {
						prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
						String payload = config.getNetworksArray().getJSONObject(pos).getString("Payload");
						edit.putString(Settings.CUSTOM_PAYLOAD_KEY, payload);
						edit.apply();
					}
				}
			}
            
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	private void loadServerData() {
		try {
			SharedPreferences prefs = mConfig.getPrefsPrivate();
			SharedPreferences.Editor edit = prefs.edit();
			//    int modeType = prefs.getInt("TunnelMode",modeGroup.getCheckedRadioButtonId());
            int pos1 = serverSpinner.getSelectedItemPosition();
            int pos2 = payloadSpinner.getSelectedItemPosition();
            boolean directModeType = config.getNetworksArray().getJSONObject(pos2).getBoolean("isSSL");
            boolean sshssltype = config.getNetworksArray().getJSONObject(pos2).getBoolean("wsPayload");
			boolean remotessltype = config.getNetworksArray().getJSONObject(pos2).getBoolean("remoteHost");
			boolean slowdnstype = config.getNetworksArray().getJSONObject(pos2).getBoolean("SlowDns");
            if (directModeType) {
                String ssl_port = config.getServersArray().getJSONObject(pos1).getString("SSLPort");
                edit.putString(Settings.SERVIDOR_PORTA_KEY, ssl_port);

			} else if (sshssltype) {
				String ssl_port1 = config.getServersArray().getJSONObject(pos1).getString("SSLPort");
				edit.putString(Settings.SERVIDOR_PORTA_KEY, ssl_port1);

			} else if (remotessltype) {
				String ssl_port2 = config.getServersArray().getJSONObject(pos1).getString("SSLPort");
				edit.putString(Settings.SERVIDOR_PORTA_KEY, ssl_port2);

			} else if (slowdnstype) {
				edit.putString(Settings.SERVIDOR_KEY, "127.0.0.1");
				edit.putString(Settings.SERVIDOR_PORTA_KEY, "2222");
            } else {
                String ssh_port = config.getServersArray().getJSONObject(pos1).getString("ServerPort");
                edit.putString(Settings.SERVIDOR_PORTA_KEY, ssh_port);
            }
			String ssh_server = config.getServersArray().getJSONObject(pos1).getString("ServerIP");
			String remote_proxy = config.getServersArray().getJSONObject(pos1).getString("ProxyIP");
			String proxy_port = config.getServersArray().getJSONObject(pos1).getString("ProxyPort");
            String ssh_user = config.getServersArray().getJSONObject(pos1).getString("ServerUser");
            String ssh_password = config.getServersArray().getJSONObject(pos1).getString("ServerPass");
            edit.putString(Settings.USUARIO_KEY, ssh_user);
            edit.putString(Settings.SENHA_KEY, ssh_password);
			edit.putString(Settings.SERVIDOR_KEY, ssh_server);
			edit.putString(Settings.PROXY_IP_KEY, remote_proxy);
			edit.putString(Settings.PROXY_PORTA_KEY, proxy_port);
			edit.apply();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadServer() {
		try {
			if (serverList.size() > 0) {
				serverList.clear();
				serverAdapter.notifyDataSetChanged();
			}
			for (int i = 0; i < config.getServersArray().length(); i++) {
				JSONObject obj = config.getServersArray().getJSONObject(i);
				serverList.add(obj);
				serverAdapter.notifyDataSetChanged();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadNetworks1() {
		try {
			if (payloadList.size() > 0) {
				payloadList.clear();
				payloadAdapter.clear();
			}
			JSONObject obj = getJSONConfig2(this);
			JSONArray networkPayload = obj.getJSONArray("Networks");
			for (int i = 0; i < networkPayload.length(); i++) {
				payloadList.add(networkPayload.getJSONObject(i));
			}
			//Collections.sort(listNetwork, NetworkNameComparator());
			payloadAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Toast.makeText(SocksHttpMainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void loadNetworks() {
		try {
			if (payloadList.size() > 0) {
				payloadList.clear();
				payloadAdapter.notifyDataSetChanged();
			}
			for (int i = 0; i < config.getNetworksArray().length(); i++) {
				JSONObject obj = config.getNetworksArray().getJSONObject(i);
				payloadList.add(obj);
				payloadAdapter.notifyDataSetChanged();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateConfig(final boolean isOnCreate) {
		new ConfigUpdate(this, new ConfigUpdate.OnUpdateListener() {
			@Override
			public void onUpdateListener(String result) {
				try {
					if (!result.contains("Error on getting data")) {
						String json_data = AESCrypt.decrypt(config.PASSWORD, result);
						if (isNewVersion(json_data)) {
							newUpdateDialog(result);
						} else {
							if (!isOnCreate) {
								noUpdateDialog();
							}
						}
					} else if(result.contains("Error on getting data") && !isOnCreate){
						errorUpdateDialog(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start(isOnCreate);
	}

	private boolean isNewVersion(String result) {
		try {
			String current = config.getVersion();
			String update = new JSONObject(result).getString("Version");
			return config.versionCompare(update, current);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void newUpdateDialog(final String result) {
		new AlertDialog.Builder(SocksHttpMainActivity.this)
				.setTitle("New Update Available")
				.setMessage("There is a new config update found. Would you like to update your config?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							File file = new File(getFilesDir(), "Config.json");
							OutputStream out = new FileOutputStream(file);
							out.write(result.getBytes());
							out.flush();
							out.close();
							restart_app();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton("No", null)
				.create().show();
	}

	private void noUpdateDialog() {
		new AlertDialog.Builder(SocksHttpMainActivity.this)
				.setTitle("No Update Available")
				.setMessage("There is a no new update found.")
				.setPositiveButton("Ok",null)
				.create().show();
	}

	private void errorUpdateDialog(String error) {
		new AlertDialog.Builder(SocksHttpMainActivity.this)
				.setTitle("Error on update")
				.setMessage("There is an error occurred when checking for update. Please contact Developer.\n" +
						"Error:" + error)
				.setPositiveButton("Ok", null)
				.create().show();
	}

	private void restart_app() {
		Intent intent = new Intent(this, SocksHttpMainActivity.class);
		int i = 123456;
		PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + ((long) 1000), pendingIntent);
		finish();
	}
	
	public void offlineUpdate() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		startActivityForResult(intent, PICK_FILE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_FILE)
		{
			if (resultCode == RESULT_OK) {
				try {
					Uri uri = data.getData();
					String intentData = importer(uri);
					//String cipter = AESCrypt.decrypt(ConfigUtil.PASSWORD, intentData);
					File file = new File(getFilesDir(), "Config.json");
					OutputStream out = new FileOutputStream(file);
					out.write(intentData.getBytes());
					out.flush();
					out.close();
					loadServer();
					loadNetworks();
					restart_app();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String importer(Uri uri)
	{
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		try
		{
			reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

			String line = "";
			while ((line = reader.readLine()) != null)
			{
				builder.append(line);
			}
			reader.close();
		}
		catch (IOException e) {e.printStackTrace();}
		return builder.toString();
	} 
	/**
	 * Tunnel SSH
	 */

	private void startTimer() {
        if (TunnelUtils.isNetworkOnline(this)) {
			countDown = new CountDownTimer(55000, 1000) {

				public void onTick(long millisUntilFinished) {
					//totalSecs.setText(millisUntilFinished / 1000+"s");
				}

				public void onFinish() {
					// Toast.makeText(SocksHttpMainActivity.this, "Reconnecting",Toast.LENGTH_LONG).show();
					Intent reconTunnel = new Intent(SocksHttpService.TUNNEL_SSH_RESTART_SERVICE);
					LocalBroadcastManager.getInstance(SocksHttpMainActivity.this)
						.sendBroadcast(reconTunnel);
					SkStatus.clearLog();
					start();
				}
			}.start();
		}
    }

    private void pauseTimer() {

        countDown.cancel();

	}
	 
	public void startOrStopTunnel(Activity activity) {
		if (SkStatus.isTunnelActive()) {
            SharedPreferences prefs = mConfig.getPrefsPrivate();
			TunnelManagerHelper.stopSocksHttp(activity);
			this.startService(new Intent(this, InjectorService.class).setAction("STOP"));

		}
		else {
			// oculta teclado se vísivel, tá com bug, tela verde
			//Utils.hideKeyboard(activity);
			if(startSSHCheckbox.isChecked()){
				this.startService(new Intent(this, InjectorService.class).setAction("START"));
				Toast.makeText(SocksHttpMainActivity.this, "Start SSH is running",
							   Toast.LENGTH_SHORT).show();
			}else{
				this.startService(new Intent(this, InjectorService.class).setAction("STOP"));
				Toast.makeText(SocksHttpMainActivity.this, "Start SSH is off",
							   Toast.LENGTH_SHORT).show();
			}
			Settings mConfig = new Settings(activity);
			if (mConfig.getPrefsPrivate()
				.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				if (inputPwUser.getText().toString().isEmpty() || 
					inputPwPass.getText().toString().isEmpty()) {
					Toast.makeText(this, R.string.error_userpass_empty, Toast.LENGTH_SHORT)
						.show();
					return;
				}
			}
	
			
			Intent intent = new Intent(activity, LaunchVpn.class);
			intent.setAction(Intent.ACTION_MAIN);

			if (mConfig.getHideLog()) {
				intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
			}

			activity.startActivity(intent);
		}
	}
	
	private void setPayloadSwitch(int tunnelType, boolean isCustomPayload) {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();

		customPayloadSwitch.setChecked(isCustomPayload);

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			payloadEdit.setEnabled(false);
			
			if (mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY).isEmpty()) {
				customPayloadSwitch.setEnabled(false);
			}
			else {
				customPayloadSwitch.setEnabled(!isRunning);
			}
			
			if (!isCustomPayload && tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY)
				payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
			else
				payloadEdit.setText("*******");
		}
		else {
			customPayloadSwitch.setEnabled(!isRunning);

			if (isCustomPayload) {
				payloadEdit.setText(mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY));
				payloadEdit.setEnabled(!isRunning);
			}
			else if (tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
				payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
				payloadEdit.setEnabled(true);
			}
		}

		if (isCustomPayload || tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
			payloadLayout.setVisibility(View.VISIBLE);
		}
		else {
			payloadLayout.setVisibility(View.GONE);
		}
	}

	public void setStarterButton(Button starterButton, Activity activity) {
		String state = SkStatus.getLastState();
		boolean isRunning = SkStatus.isTunnelActive();

		if (starterButton != null) {
			int resId;
			
			SharedPreferences prefsPrivate = new Settings(activity).getPrefsPrivate();

			if (ConfigParser.isValidadeExpirou(prefsPrivate
					.getLong(Settings.CONFIG_VALIDADE_KEY, 0))) {
				resId = R.string.expired;
				starterButton.setEnabled(false);

				if (isRunning) {
					startOrStopTunnel(activity);
				}
			}
			else if (prefsPrivate.getBoolean(Settings.BLOQUEAR_ROOT_KEY, false) &&
					ConfigParser.isDeviceRooted(activity)) {
			   resId = R.string.blocked;
			   starterButton.setEnabled(false);
			   
			   Toast.makeText(activity, R.string.error_root_detected, Toast.LENGTH_SHORT)
					.show();

			   if (isRunning) {
				   startOrStopTunnel(activity);
			   }
			}
			else if (SkStatus.SSH_INICIANDO.equals(state)) {
				resId = R.string.stop;
				starterButton.setEnabled(false);
			}
			else if (SkStatus.SSH_PARANDO.equals(state)) {
				resId = R.string.state_stopping;
				starterButton.setEnabled(false);
			}
			else {
				resId = isRunning ? R.string.stop : R.string.start;
				starterButton.setEnabled(true);
			}

			starterButton.setText(resId);
		}
	}
	
	private boolean isMostrarSenha = false;
	
	@Override
	public void onClick(View p1)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		switch (p1.getId()) {
			case R.id.activity_starterButtonMain:
				doSaveData();
				loadServerData();
				startOrStopTunnel(this);
				break;

			case R.id.activity_mainInputProxyLayout:
				break;
			case R.id.useDns:
				if(dnsCheckBox.isChecked()){
					Snackbar.make(p1, Html.fromHtml("<font color='#0A87FB'>Improved privacy and bypass Internet Censorship</font>"), Snackbar.LENGTH_LONG).setAction("SET DNS", new OnClickListener()
						{

							@Override
							public void onClick(View p1)
							{
								SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
								if(dnsCheckBox.getText().toString().equals("DNS (Default DNS)")){
									mPref.edit().putBoolean("Default_dns",true).apply();
									mPref.edit().putBoolean("Google_dns",false).apply();
									mPref.edit().putBoolean("Primary_dns",false).apply();
								}else if(dnsCheckBox.getText().toString().equals("DNS (Google DNS)")){
									mPref.edit().putBoolean("Default_dns",false).apply();
									mPref.edit().putBoolean("Google_dns",true).apply();
									mPref.edit().putBoolean("Primary_dns",false).apply();
									mPref.edit().putString(Settings.DNSRESOLVER_KEY1,"8.8.8.8").apply();
									mPref.edit().putString(Settings.DNSRESOLVER_KEY2,"8.8.4.4").apply();
								}else if(dnsCheckBox.getText().toString().equals("DNS (Primary DNS)")){
									mPref.edit().putBoolean("Default_dns",false).apply();
									mPref.edit().putBoolean("Google_dns",false).apply();
									mPref.edit().putBoolean("Primary_dns",true).apply();

								}else{
									mPref.edit().putBoolean(Settings.DNSFORWARD_KEY,false).apply();
									mPref.edit().putString(Settings.DNSTYPE_KEY, Settings.DNS_DEFAULT_KEY).apply();
									//custom.setText(mPref.getString(Settings.DNSTYPE_KEY,Settings.DNS_DEFAULT_KEY));
								}
								Intent TunnDNS = new Intent(SocksHttpMainActivity.this, CustomDNS.class);
								startActivity(TunnDNS);
							}


						}).show();
				} else {
					Toast.makeText(SocksHttpMainActivity.this, "DNS (Google DNS) is off",
								   Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.activity_mainAutorText:
				String url = "http://t.me/SlipkProjects";
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, getText(R.string.open_with)));
				break;
				
			case R.id.activity_mainInputShowPassImageButton:
				isMostrarSenha = !isMostrarSenha;
				if (isMostrarSenha) {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_black_24dp));
				}
				else {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_off_black_24dp));
				}
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2)
	{
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		switch (p1.getCheckedRadioButtonId()) {
			case R.id.activity_mainSSHDirectRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
				proxyInputLayout.setVisibility(View.GONE);
				break;

			case R.id.activity_mainSSHProxyRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY);
				proxyInputLayout.setVisibility(View.VISIBLE);
				break;
		}

		edit.apply();

		doSaveData();
		doUpdateLayout();
	}

 
	protected void showBoasVindas() {
		new AlertDialog.Builder(this)
            . setTitle(R.string.attention)
            . setMessage(R.string.first_start_msg)
			. setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int p) {
					// ok
				}
			})
			. setCancelable(false)
            . show();
	}
	
	@Override
    public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent)
    {
        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    doUpdateLayout();
                    if (SkStatus.isTunnelActive()){

                        if (level.equals(ConnectionStatus.LEVEL_CONNECTED)){
                            connectionStatus.setText(R.string.connected);
                       
                        }

                        if (level.equals(ConnectionStatus.LEVEL_NOTCONNECTED)){
                            connectionStatus.setText(R.string.servicestop);
                        }   

                        if (level.equals(ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED)){
                            connectionStatus.setText(R.string.authenticating);
                        }       

                        if (level.equals(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET)){
                            connectionStatus.setText(R.string.connecting);
                        }           
                        if (level.equals(ConnectionStatus.LEVEL_AUTH_FAILED)){
                            connectionStatus.setText(R.string.authfailed);
                        }                   
                        if (level.equals(ConnectionStatus.UNKNOWN_LEVEL)){
                            connectionStatus.setText(R.string.disconnected);
                        }               
                        //if (level.equals(ConnectionStatus.LEVEL_RECONNECTING)){
                        //      status.setText(R.string.reconnecting);
                    }               
                    if (level.equals(ConnectionStatus.LEVEL_NONETWORK)){
                        connectionStatus.setText(R.string.nonetwork);
                    }           
                }
            });
		
		switch (state) {
			case SkStatus.SSH_CONECTADO:
				// carrega ads banner
				if (adsBannerView != null && TunnelUtils.isNetworkOnline(SocksHttpMainActivity.this)) {
					adsBannerView.setAdListener(new AdListener() {
						@Override
						public void onAdLoaded() {
							if (adsBannerView != null && !isFinishing()) {
								adsBannerView.setVisibility(View.VISIBLE);
							}
						}
					});
					adsBannerView.postDelayed(new Runnable() {
						@Override
						public void run() {
							// carrega ads interestitial
							AdsManager.newInstance(getApplicationContext())
								.loadAdsInterstitial();
							// ads banner
							if (adsBannerView != null && !isFinishing()) {
								adsBannerView.loadAd(new AdRequest.Builder()
									.build());
							}
						}
					}, 5000);
				}
			break;
		}
	}


	/**
	 * Recebe locais Broadcast
	 */

	private BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(UPDATE_VIEWS) && !isFinishing()) {
				doUpdateLayout();
			}
			
        }
    };


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Menu Itens
		switch (item.getItemId()) {
			case R.id.Share_Logs:
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT,SkStatus.CopyLogs());
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, SkStatus.CopyLogs());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
				break;

			case R.id.CopyLogs:
				ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Log Entry", SkStatus.CopyLogs());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(this, "Copied Entry", Toast.LENGTH_SHORT).show();

				break;

				// logs opções
			case R.id.miLimparLogs:
				mLogAdapter.clearLog();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		
		
			showExitDialog();
		
	}

	@Override
    public void onResume() {
        super.onResume();
		
		//doSaveData();
		//doUpdateLayout();
		
		SkStatus.addStateListener(this);
		new Timer().schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								updateHeaderCallback();
								// TODO: Implement this method
							}
						});
					// TODO: Implement this method
				}
			}, 0,1000);
		if (adsBannerView != null) {
			adsBannerView.resume();
		}
    }

	@Override
	protected void onPause()
	{
		super.onPause();
		
		doSaveData();
		
		SkStatus.removeStateListener(this);
		
		if (adsBannerView != null) {
			adsBannerView.pause();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		LocalBroadcastManager.getInstance(this)
			.unregisterReceiver(mActivityReceiver);
			
		if (adsBannerView != null) {
			adsBannerView.destroy();
		}
	}


	/**
	 * DrawerLayout Listener
	 */

	/**
	 * Utils
	 */

	public static void updateMainViews(Context context) {
		Intent updateView = new Intent(UPDATE_VIEWS);
		LocalBroadcastManager.getInstance(context)
			.sendBroadcast(updateView);
	}
	
	public void showExitDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).
			create();
		dialog.setTitle(getString(R.string.attention));
		dialog.setMessage(getString(R.string.alert_exit));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.
				string.exit),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Utils.exitAll(SocksHttpMainActivity.this);
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.
				string.minimize),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// minimiza app
					Intent startMain = new Intent(Intent.ACTION_MAIN);
					startMain.addCategory(Intent.CATEGORY_HOME);
					startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(startMain);
				}
			}
		);

		dialog.show();
	}
}


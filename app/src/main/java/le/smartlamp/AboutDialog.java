package le.smartlamp;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class AboutDialog extends Dialog/*AlertDialog*/{

    private TextView mVersionText;
    private TextView mCpyRightText;
	private TextView mAboutText;
	private static Context mContext = null;
	
	public AboutDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.dialog_about);
		
        mVersionText = (TextView) findViewById(R.id.version);
        mCpyRightText = (TextView) findViewById(R.id.copy);
        mAboutText = (TextView) findViewById(R.id.info); 
        
		PackageInfo pi = null;
		try {
			pi = mContext.getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		setTitle("About");
		
		StringBuilder stringbuilder = new StringBuilder("Version ");
        mVersionText.setText(stringbuilder.append(pi.versionName));

        mCpyRightText.setText("Copyright © 2015");
        
        mAboutText.setText(Html.fromHtml("<b>Developed by:</b> Trung Huynh (huynh.trung@outlook.com)" +
				"<br><br>Licensed under the Apache License, Version 2.0."));
	}
}

/*public class AboutBox {
	static String VersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			return "Unknown";
		}
	}

	public static void Show(Activity callingActivity) {
		// Use a Spannable to allow for links highlighting
		SpannableString aboutText = new SpannableString("Version "
				+ VersionName(callingActivity) + "\n\n"
				+ callingActivity.getString(R.string.about));
		// Generate views to pass to AlertDialog.Builder and to set the text

		View about;
		TextView tvAbout;
		try {
			// Inflate the custom view
			LayoutInflater inflater = callingActivity.getLayoutInflater();
			about = inflater.inflate(R.layout.dialog_about,
					(ViewGroup) callingActivity.findViewById(R.id.aboutView));
			tvAbout = (TextView) about.findViewById(R.id.aboutText);
		} catch (InflateException e) {
			// Inflater can throw exception, unlikely but default to TextView if
			// it occurs
			about = tvAbout = new TextView(callingActivity);
		}

		// Set the about text
		tvAbout.setText(aboutText);

		// Now Linkify the text
		Linkify.addLinks(tvAbout, Linkify.ALL);

		// Build and show the dialog
		new AlertDialog.Builder(callingActivity)
				.setTitle("About " + callingActivity.getString(R.string.app_name))
				.setCancelable(true)
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", null)
				.setView(about)
				.show();
	}
}*/

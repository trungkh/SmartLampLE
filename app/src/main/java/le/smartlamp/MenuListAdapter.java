package le.smartlamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {

	Context context;
	String[] mTitle;

	public MenuListAdapter(Context context, String[] title) {
		this.context = context;
		this.mTitle = title;
	}

	@Override
	public int getCount() {
		return mTitle.length;
	}

	@Override
	public Object getItem(int position) {
		return mTitle[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.menu_list_drawer, parent,false);
		int[] icon = new int[] { R.drawable.connect, R.drawable.disconnect,  R.drawable.setting};
		
		TextView txtTitle = (TextView) itemView.findViewById(R.id.title_text);

		ImageView imgIcon = (ImageView) itemView.findViewById(R.id.icon);

		txtTitle.setText(mTitle[position]);
		imgIcon.setImageResource(icon[position]);

		return itemView;
	}
}

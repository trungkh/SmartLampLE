/*
 * Copyright 2015 Trung Huynh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        TextView txtTitle = itemView.findViewById(R.id.title_text);

        ImageView imgIcon = itemView.findViewById(R.id.icon);

        txtTitle.setText(mTitle[position]);
        imgIcon.setImageResource(icon[position]);

        return itemView;
    }
}

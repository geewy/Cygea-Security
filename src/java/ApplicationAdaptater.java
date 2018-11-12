package com.example.geewy.cygea;



import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by geewy on 05/12/2016.
 */
public class ApplicationAdaptater extends ArrayAdapter<ApplicationInfo>{

    private List<ApplicationInfo> objects = null;
    private Context context;
    private PackageManager packageManager;

    public ApplicationAdaptater(Context context, int resource, List<ApplicationInfo> objects) {
        super(context, resource, objects);

        this.context = context;
        this.objects = objects;

        packageManager = context.getPackageManager();

    }

    @Override
    public int getCount(){
        return((null != objects) ? objects.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return((null != objects) ? objects.get(position) : null);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null)
        {
            LayoutInflater layoutInFlater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInFlater.inflate(R.layout.list_row, null);
        }

        ApplicationInfo data = objects.get(position);

        if(data != null)
        {
            TextView appName = (TextView)view.findViewById(R.id.app_name);
            TextView packageName = (TextView)view.findViewById(R.id.app_package);
            ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);

            appName.setText(data.loadLabel(packageManager));
            packageName.setText(data.packageName);
            appIcon.setImageDrawable(data.loadIcon(packageManager));
        }
        return view;
    }
}

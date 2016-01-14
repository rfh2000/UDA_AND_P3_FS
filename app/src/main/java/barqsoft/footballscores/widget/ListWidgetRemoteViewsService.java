package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by R.Pendlebury on 13/01/2016.
 */
public class ListWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = ListWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {


            private Cursor data = null;
            private static final int NUM_DAYS = 5;
            private String[] searchDate = new String[NUM_DAYS];
            private int mCount;
            private boolean hasResultHeader = false;
            private static final int manchesterUnited = R.drawable.manchester_united;
            private List<String> matchList;
            static final int INDEX_SCORES_ID = 0;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
//                if (data != null) {
//                    data.close();
//                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();
                matchList = queryData();
                mCount = matchList.size();
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
//                if (data != null) {
//                    data.close();
//                    data = null;
//                }
                matchList.clear();
            }

            @Override
            public int getCount() {
                //return data == null ? 0 : data.getCount();
                return mCount;
            }

            @Override
            public RemoteViews getViewAt(int position) {

//                if (position == AdapterView.INVALID_POSITION ||
//                        data == null || !data.moveToPosition(position)) {
//                    Log.v(LOG_TAG, "Returned null after position " + position);
//                    return null;
//                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.stack_widget_item);

                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                //    setRemoteContentDescription(views, description);
                //}
                views.setTextViewText(R.id.widget_item, matchList.get(position));
//                if (matchList.get(position).equals("UPCOMING FIXTURES") || matchList.get(position).equals("PREVIOUS RESULTS")) {
//                    views.setTextColor(R.id.widget_item, getResources().getColor(R.color.orange07));
//
//                }
                final Intent fillInIntent = new Intent();
                Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

                //fillInIntent.setData(weatherUri);
                //views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                views.setOnClickFillInIntent(R.id.widget_item, launchIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                //views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.stack_widget_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
//                if (data.moveToPosition(position))
//                    return data.getLong(INDEX_SCORES_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            private List<String> queryData() {

                List<String> matchList = new ArrayList<>();

                for (int i = 0;i < NUM_DAYS;i++)
                {
                    Date tmpDate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    searchDate[i] = simpleDateFormat.format(tmpDate);
                }

                matchList.add("UPCOMING FIXTURES");
                for (int i = searchDate.length-1 ; i > -1 ; i--){
                    String[] tmpDate = new String[1];
                    tmpDate[0] = searchDate[i];
                    data = getContentResolver().query(
                            DatabaseContract.scores_table.buildScoreWithDate(),
                            null,
                            null,
                            tmpDate,
                            null);

                    if (data != null && data.getCount() > 0) {
                        if (data.moveToFirst()) {
                            do {
                                mCount++;
                                String midString = "";
                                if (data.getString(6).equals("-1") && data.getString(7).equals("-1")){
                                    midString = "v";
                                } else {
                                    if (!hasResultHeader) {
                                        matchList.add("PREVIOUS RESULTS");
                                        hasResultHeader = true;
                                        mCount++;
                                    }
                                    midString =  data.getString(6) + " - " + data.getString(7);
                                }
                                String s = data.getString(3) +
                                        " " +
                                        midString +
                                        " " + data.getString(4);
                                matchList.add(s);
                            } while (data.moveToNext());
                        }
                    }
                    data.close();
                }
                return matchList;
            }
        };
    }
}


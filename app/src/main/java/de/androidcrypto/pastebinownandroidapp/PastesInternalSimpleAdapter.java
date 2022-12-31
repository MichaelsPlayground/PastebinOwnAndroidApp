package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PastesInternalSimpleAdapter extends RecyclerView.Adapter<PastesInternalSimpleAdapter.MyViewHolder> {

    private static final String TAG = "PastesInternalSimpleAdapter";

    private ArrayList<String> mPasteList;
    private Context mContext;
    private boolean mEncryptedPastes;

    //public PastesAdapter(String data) {
    public PastesInternalSimpleAdapter(ArrayList<String> pasteList, Context context, boolean pasteIsEncrypted) {
        //super();

        System.out.println("### PastesInternalSimpleAdapter INIT ###");
        System.out.println("*** PastesAdapter received items: " + pasteList.size());
        this.mPasteList = pasteList;
        this.mContext = context;
        this.mEncryptedPastes = pasteIsEncrypted;
    }

    @Override
    public int getItemCount() {
        //return nList.getLength();
        return mPasteList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public PastesInternalSimpleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_home_pastes, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //PasteModel pasteModel = mPasteList.get(position);
        String filename = mPasteList.get(position);
        System.out.println("** onBindViewHolder pos " + position + " fn " + filename);
        holder.paste_title.setText(filename);
        //holder.paste_hits.setText(String.valueOf(pasteModel.getPasteHits()));
        //holder.paste_hits.setText("17");
        holder.paste_expire.setText("exp");
        // no data available for this field
        //holder.paste_size.setText(String.valueOf(pasteModel.getPasteSize()));
        String expDate = "no exp";
        /*
        //PasteExpireDate ped = new PasteExpireDate(pasteModel.g);
        System.out.println("expDate: " + expDate);
        if (expDate.equals("NEVER")) {
            holder.paste_expire.setText("No expiry");
        } else {
            holder.paste_expire.setText("Expires within " + expDate);
            //Timestamp stamp = new Timestamp(Integer.parseInt(expDate));
            //Date date = new Date(stamp.getTime());
            //holder.paste_expire.setText("Expire on " + date);
        }

         */
        /*
        if (pasteModel.getPastePrivate() == 0) {
            // public
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_lock_open_24));
        } else if (pasteModel.getPastePrivate() == 1) {
            // unlisted
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_playlist_remove_24));
        } else {
            // private
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_lock_24));
        }

         */
        //Timestamp stamp = new Timestamp(Integer.parseInt(pasteModel.));
        //Date date = new Date(pasteModel.getPasteDate());

        // todo get data from file like timestamp, length, PasteLink, Private/Public etc

        //holder.paste_date.setText(pasteModel.getPasteDate().toString());
        //holder.paste_link.setText(pasteModel.getPasteUrl());

        // todo onRecyclerview click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "recyclerView onClickListener");
                /*
                Log.i(TAG, "pasteUrl: " + mPasteList.get(holder.getAdapterPosition()).getPasteUrl());
                Intent intent = new Intent(mContext, ViewPasteActivity.class);
                intent.putExtra("PASTE_URL", mPasteList.get(holder.getAdapterPosition()).getPasteUrl());
                intent.putExtra("USER_KEY", mUserKey);
                mContext.startActivity(intent);

                 */
            }
        });


        /*
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO change to go to another activity NOT MainActivity
                Intent i = new Intent(mContext, MainActivity.class);
                i.putExtra("paste_id", getValue("paste_key", element));
                try {
                    i.putExtra("paste_name", getValue("paste_title", element));
                } catch (Exception e) {
                    i.putExtra("paste_name", "View Paste");
                }
                boolean trends = false;
                if (!trends) {
                    i.putExtra("mine", true);
                }
                startActivity(i);
            }
        });

        holder.paste_title.setText(getValue("paste_title", element));

        if (position % 4 == 0 && holder.adcontainer.getChildCount() < 1) {

        }

    } catch(
    Exception e)

    {
        holder.paste_title.setText("No title.");
    }
}*/

    }

public static class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView paste_title, paste_expire;
    public TextView paste_date, paste_link;
    LinearLayout container;
    ImageView private_ind;

    public MyViewHolder(View itemView) {
        super(itemView);
        paste_title = (TextView) itemView.findViewById(R.id.paste_title);
        paste_expire = (TextView) itemView.findViewById(R.id.paste_expire);
        paste_date = itemView.findViewById(R.id.paste_date);
        paste_link = itemView.findViewById(R.id.paste_link);
        container = (LinearLayout) itemView.findViewById(R.id.llcontainer);
        private_ind = (ImageView) itemView.findViewById(R.id.private_ind);
    }
}
}

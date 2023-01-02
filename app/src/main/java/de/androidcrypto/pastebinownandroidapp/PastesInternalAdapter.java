package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PastesInternalAdapter extends RecyclerView.Adapter<PastesInternalAdapter.MyViewHolder> {

    private static final String TAG = "PastesInternalAdapter";

    private ArrayList<FileModel> mPasteList;
    private Context mContext;
    private boolean mEncryptedPastes;

    //public PastesAdapter(String data) {
    public PastesInternalAdapter(ArrayList<FileModel> pasteList, Context context, boolean pasteIsEncrypted) {
        //super();

        System.out.println("### PastesInternalAdapter INIT ###");
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
    public PastesInternalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_internal_pastes, parent, false);
        // View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_home_pastes, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //PasteModel pasteModel = mPasteList.get(position);
        FileModel fileModel = mPasteList.get(position);
        String filename = fileModel.getFileName();
        System.out.println("** onBindViewHolder pos " + position + " fn " + filename);
        holder.paste_title.setText(filename);
        holder.paste_date.setText(fileModel.getDate().toString());
        //holder.paste_hits.setText(String.valueOf(pasteModel.getPasteHits()));
        //holder.paste_hits.setText("17");
        holder.paste_expire.setText("");
        // no data available for this field
        //holder.paste_size.setText(String.valueOf(pasteModel.getPasteSize()));
        String expDate = "no exp";

        holder.paste_link.setText(fileModel.getUrl());

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

        if (fileModel.getVisibilityType().equals(InternalStorageUtils.VISIBILITY_TYPE_PUBLIC)) {
            // public
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_visibility_24));
        /*
        } else if (pasteModel.getPastePrivate() == 1) {
            // unlisted
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_playlist_remove_24));*/
        } else {
            // private
            holder.private_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_visibility_off_24));
        }
        if (fileModel.getContentType().equals(InternalStorageUtils.ENCRYPTED_CONTENT)) {
            holder.encrypt_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_lock_24));
        } else {
            holder.encrypt_ind.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_lock_open_24));
        }


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

                Log.i(TAG, "pasteFilename: " + mPasteList.get(holder.getAdapterPosition()).getFileName());
                Intent intent = new Intent(mContext, ViewInternalPasteActivity.class);
                intent.putExtra("FILENAME", mPasteList.get(holder.getAdapterPosition()).getFileName());
                intent.putExtra("FILENAME_STORAGE", mPasteList.get(holder.getAdapterPosition()).getFileNameStorage());
                intent.putExtra("TIMESTAMP", String.valueOf(mPasteList.get(holder.getAdapterPosition()).getTimestamp()));
                intent.putExtra("VISIBILITY_TYPE", mPasteList.get(holder.getAdapterPosition()).getVisibilityType());
                intent.putExtra("CONTENT_TYPE", mPasteList.get(holder.getAdapterPosition()).getContentType());
                mContext.startActivity(intent);



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
    ImageView private_ind, encrypt_ind;

    public MyViewHolder(View itemView) {
        super(itemView);
        paste_title = (TextView) itemView.findViewById(R.id.paste_title);
        paste_expire = (TextView) itemView.findViewById(R.id.paste_expire);
        paste_date = itemView.findViewById(R.id.paste_date);
        paste_link = itemView.findViewById(R.id.paste_link);
        container = (LinearLayout) itemView.findViewById(R.id.llcontainer);
        private_ind = (ImageView) itemView.findViewById(R.id.private_ind);
        encrypt_ind = itemView.findViewById(R.id.encrypt_ind);
    }
}
}

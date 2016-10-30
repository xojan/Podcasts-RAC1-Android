package cat.xojan.random1.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cat.xojan.random1.R;
import cat.xojan.random1.commons.PicassoUtil;
import cat.xojan.random1.domain.model.Section;

public class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.ViewHolder>  {

    private final List<Section> mSectionList;
    private RecyclerViewListener mListener;

    public SectionListAdapter(List<Section> sections, RecyclerViewListener listener) {
        mSectionList = sections;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new ItemClickListener(position));
        Section section = mSectionList.get(position);

        holder.title.setText(section.getTitle());
        PicassoUtil.loadImage(holder.itemView.getContext(), section.getImageDrawable(),
                holder.image, false);
    }

    @Override
    public int getItemCount() {
        return mSectionList.size();
    }

    public void destroy() {
        mListener = null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            image = (ImageView) itemView.findViewById(R.id.circle_image);
        }
    }

    public interface RecyclerViewListener {
        void onClick(Section program);
    }

    private class ItemClickListener implements View.OnClickListener {
        private final int mPosition;

        public ItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(mSectionList.get(mPosition));
        }
    }
}
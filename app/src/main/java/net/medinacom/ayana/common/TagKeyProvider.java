package net.medinacom.ayana.common;

import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;


public class TagKeyProvider<Tag extends Parcelable> extends ItemKeyProvider<Tag> {

    private final SparseArray<Tag> positionToTag = new SparseArray();
    private final Map<Tag, Integer> tagToPosition = new HashMap();
    private final RecyclerView recyclerView;

    public TagKeyProvider(RecyclerView recyclerView) {
        super(ItemKeyProvider.SCOPE_CACHED);
        this.recyclerView = recyclerView;
        this.recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                onAttached(view);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                onDetached(view);
            }
        });
    }

    protected void onAttached(View view) {
        RecyclerView.ViewHolder holder = recyclerView.findContainingViewHolder(view);
        int position = holder.getAdapterPosition();
        Tag tag = (Tag) view.getTag();
        if(position != -1 && !tag.equals(null)) {
            positionToTag.put(position, tag);
            tagToPosition.put(tag, position);
        }
    }

    protected void onDetached(View view) {
        RecyclerView.ViewHolder holder = recyclerView.findContainingViewHolder(view);
        if(holder == null) return;
        int position = holder.getAdapterPosition();
        Tag tag = (Tag) view.getTag();
        if(position != -1 && !tag.equals(null)) {
            positionToTag.delete(position);
            tagToPosition.remove(tag);
        }
    }

    @Nullable
    @Override
    public Tag getKey(int position) {
        return positionToTag.get(position, null);
    }

    @Override
    public int getPosition(@NonNull Tag key) {
        return tagToPosition.containsKey(key) ? tagToPosition.get(key) : -1;
    }
}

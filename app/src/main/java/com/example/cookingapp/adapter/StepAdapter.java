package com.example.cookingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookingapp.R;
import com.example.cookingapp.model.Step;
import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private List<Step> steps;
    private StepClickListener stepClickListener;

    public StepAdapter(List<Step> steps, StepClickListener listener) {
        this.steps = steps;
        this.stepClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Step step = steps.get(position);
        holder.textViewStep.setText(step.getStepName()); // Hiển thị tên bước

        // Xử lý sự kiện khi người dùng nhấn vào nút xóa
        holder.buttonDeleteStep.setOnClickListener(v -> {
            // Gọi phương thức xóa bước của Adapter khi nút xóa được nhấn
            deleteStep(position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (stepClickListener != null) {
                stepClickListener.onStepClicked(step.getStepDetail()); // Truyền chi tiết bước tới listener
            }
        });
    }

    // Phương thức để xóa bước khỏi danh sách
    private void deleteStep(int position) {
        steps.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStep;
        ImageView buttonDeleteStep;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStep = itemView.findViewById(R.id.step_text_view);
            buttonDeleteStep = itemView.findViewById(R.id.button_delete_step);
        }
    }

    public void updateSteps(List<Step> newSteps) {
        steps = newSteps;
        notifyDataSetChanged(); // Notify adapter about the change
    }

    public interface StepClickListener {
        void onStepClicked(String stepDetail);
    }
}

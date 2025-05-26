package com.example.myunyt;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.example.myunyt.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorAdapter.ProfessorViewHolder> {

    private List<Professor> professors;
    private List<Professor> professorsFull;
    private OnItemClickListener listener;
    private OnRatingChangedListener ratingListener;

    public interface OnRatingChangedListener {
        void onRatingChanged(int professorId, float rating);
    }

    public interface OnItemClickListener {
        void onItemClick(Professor professor);
    }

    public ProfessorAdapter(List<Professor> professors) {
        this.professors = new ArrayList<>(professors);
        this.professorsFull = new ArrayList<>(professors);
    }

    public void setOnRatingChangedListener(OnRatingChangedListener listener) {
        this.ratingListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;
    }
    public static class ProfessorViewHolder extends RecyclerView.ViewHolder {
        ImageView professorImage;
        TextView professorName;
        TextView professorCourse;
        RatingBar professorRating;
        Button submitRatingButton;

        public ProfessorViewHolder(View itemView) {
            super(itemView);
            professorImage = itemView.findViewById(R.id.professorImage);
            professorName = itemView.findViewById(R.id.professorName);
            professorCourse = itemView.findViewById(R.id.professorCourse);
            professorRating = itemView.findViewById(R.id.professorRating);
            submitRatingButton = itemView.findViewById(R.id.submitRatingButton);
        }
    }

    @NonNull
    @Override
    public ProfessorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professor, parent, false);
        return new ProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessorViewHolder holder, int position) {
        Professor professor = professors.get(position);

        if (professor == null) {
            Log.e("ProfessorAdapter", "Professor is null at position: " + position);
            resetViewHolder(holder);
            return;
        }

        holder.submitRatingButton.setOnClickListener(v -> {
            float currentRating = holder.professorRating.getRating();

            Toast.makeText(v.getContext(),
                    "Rating submitted: " + currentRating + " stars for " + professor.getName(),
                    Toast.LENGTH_SHORT).show();

            if (ratingListener != null) {
                ratingListener.onRatingChanged(professor.getId(), currentRating);
            }

            professor.setRating(currentRating);
        });

        bindProfessorData(holder, professor, position);
    }

    private void resetViewHolder(ProfessorViewHolder holder) {
        holder.professorImage.setImageResource(R.drawable.professor_placeholder);
        holder.professorName.setText("");
        holder.professorCourse.setText("");
        holder.professorRating.setRating(0);
        holder.professorRating.setOnRatingBarChangeListener(null);
        holder.submitRatingButton.setOnClickListener(null);
    }

    private void bindProfessorData(ProfessorViewHolder holder, Professor professor, int position) {
        holder.professorName.setText(professor.getName() != null ? professor.getName() : "");
        holder.professorCourse.setText(professor.getCourses() != null ? professor.getCourses() : "");

        holder.professorRating.setRating(professor.getRating());
        holder.professorRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && ratingListener != null && position != RecyclerView.NO_POSITION) {
                try {
                    ratingListener.onRatingChanged(professor.getId(), rating);
                    professors.get(position).setRating(rating);
                } catch (Exception e) {
                    Log.e("ProfessorAdapter", "Rating change error", e);
                }
            }
        });

        loadProfessorImage(holder, professor);

        setContentDescriptions(holder, professor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                try {
                    listener.onItemClick(professors.get(position));
                } catch (Exception e) {
                    Log.e("ProfessorAdapter", "Click listener error", e);
                }
            }
        });
    }

    private void loadProfessorImage(ProfessorViewHolder holder, Professor professor) {
        int imageResId = R.drawable.professor_placeholder;

        try {
            if (professor.getImageUrl() != null && !professor.getImageUrl().isEmpty()) {
                String resourceName = professor.getImageUrl().toLowerCase().trim();
                if (resourceName.contains(".")) {
                    resourceName = resourceName.substring(0, resourceName.lastIndexOf('.'));
                }
                imageResId = holder.itemView.getContext().getResources()
                        .getIdentifier(resourceName, "drawable", holder.itemView.getContext().getPackageName());

                Log.d("ImageLoad", "Resolved image resource: " + resourceName);

            }
        } catch (Exception e) {
            Log.e("ImageLoad", "Error loading image: " + professor.getImageUrl(), e);
        }

        Picasso.get()
                .load(imageResId)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(holder.professorImage);
    }

    private void setContentDescriptions(ProfessorViewHolder holder, Professor professor) {
        String name = professor.getName() != null ? professor.getName() : "Unknown professor";
        String courses = professor.getCourses() != null ? professor.getCourses() : "No courses listed";

        holder.professorImage.setContentDescription("Profile image of " + name);
        holder.professorRating.setContentDescription("Rating: " + professor.getRating() + " out of 5");
        holder.itemView.setContentDescription(
                name + ". Teaches: " + courses +
                        ". Rating: " + professor.getRating() + " out of 5"
        );
    }

    @Override
    public int getItemCount() {

        return professors.size();
    }

    public void filter(String text, String faculty) {
        List<Professor> filteredList = new ArrayList<>();

        for (Professor professor : professorsFull) {
            boolean matchesFaculty = faculty == null || faculty.isEmpty() || faculty.equals("All") ||
                    (professor.getFaculty() != null && professor.getFaculty().equals(faculty));
            boolean matchesSearch = text == null || text.isEmpty() ||
                    (professor.getName() != null && professor.getName().toLowerCase().contains(text.toLowerCase())) ||
                    (professor.getCourses() != null && professor.getCourses().toLowerCase().contains(text.toLowerCase()));

            if (matchesFaculty && matchesSearch) {
                filteredList.add(professor);
            }
        }

        updateProfessorsList(filteredList);
    }

    public void updateProfessorsList(List<Professor> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProfessorDiffCallback(professors, newList));
        professors.clear();
        professors.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    private static class ProfessorDiffCallback extends DiffUtil.Callback {
        private final List<Professor> oldList;
        private final List<Professor> newList;

        public ProfessorDiffCallback(List<Professor> oldList, List<Professor> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Professor oldProfessor = oldList.get(oldItemPosition);
            Professor newProfessor = newList.get(newItemPosition);

            return (oldProfessor.getName() != null && oldProfessor.getName().equals(newProfessor.getName())) &&
                    (oldProfessor.getCourses() != null && oldProfessor.getCourses().equals(newProfessor.getCourses())) &&
                    oldProfessor.getRating() == newProfessor.getRating() &&
                    (oldProfessor.getImageUrl() != null && oldProfessor.getImageUrl().equals(newProfessor.getImageUrl())) &&
                    (oldProfessor.getFaculty() != null && oldProfessor.getFaculty().equals(newProfessor.getFaculty()));
        }
    }
}
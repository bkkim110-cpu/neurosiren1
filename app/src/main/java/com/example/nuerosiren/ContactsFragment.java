package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private List<ContactModel> contactList = new ArrayList<>();
    private ContactAdapter contactAdapter;

    private final ActivityResultLauncher<Intent> addContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String name = result.getData().getStringExtra("contact_name");
                    String relationship = result.getData().getStringExtra("contact_relationship");
                    String phone = result.getData().getStringExtra("contact_phone");
                    
                    if (name != null) {
                        contactList.add(new ContactModel(name, relationship, phone));
                        if (contactAdapter != null) {
                            contactAdapter.notifyItemInserted(contactList.size() - 1);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> editContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int position = data.getIntExtra("contact_position", -1);
                    String action = data.getStringExtra("action");

                    if (position != -1 && action != null) {
                        if (action.equals("update")) {
                            String name = data.getStringExtra("contact_name");
                            String relationship = data.getStringExtra("contact_relationship");
                            String phone = data.getStringExtra("contact_phone");
                            
                            contactList.set(position, new ContactModel(name, relationship, phone));
                            if (contactAdapter != null) contactAdapter.notifyItemChanged(position);
                        } else if (action.equals("delete")) {
                            contactList.remove(position);
                            if (contactAdapter != null) contactAdapter.notifyItemRemoved(position);
                        }
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Click wrapper opens your separate contact configuration activity
        ConstraintLayout btnAdd = view.findViewById(R.id.btnAddNewContact);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddContactActivity.class);
                addContactLauncher.launch(intent);
            });
        }



        // Setup the Alert Recipients (Permissions) RecyclerView
        setupAlertRecipients(view);

        // Setup the Added Contacts RecyclerView
        setupAddedContacts(view);

        return view;
    }

    private void setupAddedContacts(View view) {
        RecyclerView rvAddedContacts = view.findViewById(R.id.rvAddedContacts);
        if (rvAddedContacts != null) {
            // Empty initially as requested
            contactAdapter = new ContactAdapter(contactList);
            rvAddedContacts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAddedContacts.setAdapter(contactAdapter);
        }
    }

    private void setupAlertRecipients(View view) {
        RecyclerView rvAlertRecipients = view.findViewById(R.id.rvAlertRecipients);
        if (rvAlertRecipients != null) {
            List<PermissionModel> permissions = new ArrayList<>();
            permissions.add(new PermissionModel("GPS Location Sharing", "Send live location to responders", R.drawable.right_arrow_, true));
            permissions.add(new PermissionModel("Audio Recording", "Stream audio to contacts during event", R.drawable.record, true));
            permissions.add(new PermissionModel("Health Data sharing", "Send vitals from connected devices", R.drawable.profile, false));

            rvAlertRecipients.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAlertRecipients.setAdapter(new PermissionAdapter(permissions));
        }
    }

    // ==========================================
    // RECYCLERVIEW ADAPTER & MODEL FOR CONTACTS
    // ==========================================
    private static class ContactModel {
        String name, relationship, phone;
        public ContactModel(String name, String relationship, String phone) {
            this.name = name;
            this.relationship = relationship;
            this.phone = phone;
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        private final List<ContactModel> data;
        public ContactAdapter(List<ContactModel> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ContactModel item = data.get(position);
            holder.tvName.setText(item.name);
            holder.tvRelationship.setText(item.relationship);
            holder.tvPhone.setText(item.phone);
            holder.tvInitials.setText(item.name.substring(0, 1).toUpperCase());

            holder.ivEditContact.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EditContactActivity.class);
                intent.putExtra("contact_name", item.name);
                intent.putExtra("contact_relationship", item.relationship);
                intent.putExtra("contact_phone", item.phone);
                intent.putExtra("contact_position", position);
                
                editContactLauncher.launch(intent);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRelationship, tvPhone, tvInitials;
            ImageView ivEditContact;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvContactName);
                tvRelationship = itemView.findViewById(R.id.tvContactRelationship);
                tvPhone = itemView.findViewById(R.id.tvContactPhone);
                tvInitials = itemView.findViewById(R.id.tvContactInitials);
                ivEditContact = itemView.findViewById(R.id.ivEditContact);
            }
        }
    }

    // ==========================================
    // RECYCLERVIEW ADAPTER & MODEL FOR PERMISSIONS
    // ==========================================
    private static class PermissionModel {
        String title, desc;
        int iconRes;
        boolean isChecked;

        public PermissionModel(String title, String desc, int iconRes, boolean isChecked) {
            this.title = title;
            this.desc = desc;
            this.iconRes = iconRes;
            this.isChecked = isChecked;
        }
    }

    private static class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {
        private final List<PermissionModel> data;

        public PermissionAdapter(List<PermissionModel> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission_sharing_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PermissionModel item = data.get(position);
            holder.title.setText(item.title);
            holder.desc.setText(item.desc);
            holder.icon.setImageResource(item.iconRes);
            holder.toggle.setChecked(item.isChecked);
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            ImageView icon;
            SwitchMaterial toggle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvPermissionTitle);
                desc = itemView.findViewById(R.id.tvPermissionDesc);
                icon = itemView.findViewById(R.id.ivPermissionIcon);
                toggle = itemView.findViewById(R.id.switchPermissionToggle);
            }
        }
    }
}
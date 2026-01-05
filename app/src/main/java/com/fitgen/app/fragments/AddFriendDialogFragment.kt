package com.fitgen.app.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.fitgen.app.R
import com.fitgen.app.viewmodels.FriendViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddFriendDialogFragment : DialogFragment() {

    private val viewModel: FriendViewModel by viewModels({ requireParentFragment() })

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var btnSendRequest: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var layoutUserFound: View
    private lateinit var tvUserEmail: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupObservers()
        setupListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private fun initializeViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        btnSearch = view.findViewById(R.id.btnSearch)
        btnSendRequest = view.findViewById(R.id.btnSendRequest)
        btnCancel = view.findViewById(R.id.btnCancel)
        layoutUserFound = view.findViewById(R.id.layoutUserFound)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvError = view.findViewById(R.id.tvError)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupObservers() {
        viewModel.searchedUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                layoutUserFound.visibility = View.VISIBLE
                tvUserEmail.text = "@${user.username}"
                btnSendRequest.isEnabled = true
                tvError.visibility = View.GONE
            } else {
                layoutUserFound.visibility = View.GONE
                btnSendRequest.isEnabled = false
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSearch.isEnabled = !isLoading
            btnSendRequest.isEnabled = !isLoading && viewModel.searchedUser.value != null
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                tvError.text = message
                tvError.visibility = View.VISIBLE
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                dismiss()
            }
        }
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            val friendCode = etEmail.text.toString().trim().uppercase()
            if (friendCode.isNotEmpty()) {
                tvError.visibility = View.GONE
                viewModel.searchUser(friendCode)
            }
        }

        btnSendRequest.setOnClickListener {
            val friendCode = etEmail.text.toString().trim().uppercase()
            if (friendCode.isNotEmpty()) {
                viewModel.sendFriendRequest(friendCode)
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSearchedUser()
        viewModel.clearMessages()
    }
}

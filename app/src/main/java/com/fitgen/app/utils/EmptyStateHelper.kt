package com.fitgen.app.utils

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Utility class to handle empty state views for RecyclerViews
 */
object EmptyStateHelper {
    
    /**
     * Show or hide empty state based on adapter item count
     * @param recyclerView The RecyclerView to check
     * @param emptyView The view to show when empty (e.g., TextView with message)
     * @param emptyMessage Optional custom message to display
     */
    fun handleEmptyState(
        recyclerView: RecyclerView?,
        emptyView: View?,
        emptyMessage: String? = null
    ) {
        if (recyclerView == null || emptyView == null) return
        
        val adapter = recyclerView.adapter
        val isEmpty = adapter == null || adapter.itemCount == 0
        
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            
            // Set custom message if provided and view is TextView
            if (emptyMessage != null && emptyView is TextView) {
                emptyView.text = emptyMessage
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
    
    /**
     * Register an observer to automatically update empty state when data changes
     * @param recyclerView The RecyclerView to observe
     * @param emptyView The view to show when empty
     * @param emptyMessage Optional custom message to display
     */
    fun registerEmptyStateObserver(
        recyclerView: RecyclerView?,
        emptyView: View?,
        emptyMessage: String? = null
    ) {
        if (recyclerView == null || emptyView == null) return
        
        val adapter = recyclerView.adapter ?: return
        
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                handleEmptyState(recyclerView, emptyView, emptyMessage)
            }
            
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                handleEmptyState(recyclerView, emptyView, emptyMessage)
            }
            
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                handleEmptyState(recyclerView, emptyView, emptyMessage)
            }
        }
        
        adapter.registerAdapterDataObserver(observer)
        
        // Initial check
        handleEmptyState(recyclerView, emptyView, emptyMessage)
    }
}

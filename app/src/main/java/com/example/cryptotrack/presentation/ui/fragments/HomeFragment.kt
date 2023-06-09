package com.example.cryptotrack.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.util.query
import com.example.cryptotrack.R
import com.example.cryptotrack.databinding.FragmentHomeBinding
import com.example.cryptotrack.domain.model.Coin
import com.example.cryptotrack.presentation.ui.adapters.CoinListAdapter
import com.example.cryptotrack.presentation.viewmodels.CoinListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var homeBinding: FragmentHomeBinding
    private lateinit var coinAdapter: CoinListAdapter
    private lateinit var coinDataList: List<Coin>
    private val page = 1
    private val coinListViewModel: CoinListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeBinding = FragmentHomeBinding.bind(view)
        coinAdapter = CoinListAdapter(requireContext())


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestApi()
        setUpRecyclerView()

        homeBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                createFilterList(p0!!)
                return true
            }
        })

    }
    private fun requestApi(){
        coinListViewModel.getAllCoins(page.toString())
        CoroutineScope(Dispatchers.Main).launch {
            coinListViewModel._coinListValue.collect{
                when{
                    it.isLoading->{
                        homeBinding.progressBar.visibility = View.VISIBLE
                    }
                    it.errorMessage.isNotBlank() ->{
                        homeBinding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "${it.errorMessage}", Toast.LENGTH_LONG).show()
                    }
                    it.coinList.isNotEmpty() ->{
                        homeBinding.progressBar.visibility = View.GONE
                        coinAdapter.coinDifferCallback.submitList(it.coinList)
                        coinDataList = coinAdapter.coinDifferCallback.currentList
                        Log.d("SECOND LIST", "${coinDataList}")
                    }
                }
            }

        }
    }
    private fun setUpRecyclerView() {
        homeBinding.rcvCoinList.apply {
            adapter = coinAdapter
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun createFilterList(text: String){
        val filteredList = ArrayList<Coin>()
        for (coin in coinDataList){
            if (coin.name.lowercase().contains(text.lowercase())){
                filteredList.add(coin)
            }
        }

        if (filteredList.isEmpty()){
            Toast.makeText(requireContext(), "No Items found!", Toast.LENGTH_SHORT).show()
        }
        else{
            coinAdapter.coinDifferCallback.submitList(filteredList as List<Coin>)
        }
    }
}



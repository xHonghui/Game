package com.nft.quizgame.function.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.nft.quizgame.R

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2020/1/17
 * @tips 这个类是Object的子类
 * @fuction
 */
class WithdrawItemAdapter :
    RecyclerView.Adapter<WithdrawItemAdapter.Holder>() {
    private var data: List<WithdrawItem>? = null
    fun getData(): List<WithdrawItem>? {
        return data
    }

    fun setData(data: List<WithdrawItem>?) {
        this.data = data
    }

    var checkListener: OnCheckListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_withdraw, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        val item: WithdrawItem = data!![position]
        holder.itemTitle.text = item.title
        holder.itemDesc.text = item.desc
        if (item.isCheck) {
            holder.itemContent.isSelected = true
            holder.itemNewuser.isSelected = true
            holder.itemTitle.isSelected = true
            holder.itemDesc.isSelected = true
        } else {
            holder.itemNewuser.isSelected = false
            holder.itemContent.isSelected = false
            holder.itemTitle.isSelected = false
            holder.itemDesc.isSelected = false
        }

        if (item.isNewUser) {
            holder.itemNewuser.visibility = View.VISIBLE
        } else {
            holder.itemNewuser.visibility = View.GONE
        }

        holder.itemContent.setOnClickListener {
            if (checkListener != null) {
                checkItem(position)
            }
        }
    }

    fun checkItem(position: Int) {
        for (i in 0 until data!!.size) {
            if (i == position) {
                data!![i].isCheck = (true)
            } else {
                data!![i].isCheck = (false)
            }
        }
        notifyDataSetChanged()
        checkListener!!.onCheck(data, position)
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTitle: TextView
        var itemDesc: TextView
        var itemContent: ConstraintLayout
        var itemNewuser: TextView

        init {
            itemTitle = itemView.findViewById(R.id.item_title)
            itemDesc = itemView.findViewById(R.id.item_desc)
            itemNewuser = itemView.findViewById(R.id.item_newuser)
            itemContent = itemView.findViewById(R.id.item_content)
        }
    }

    interface OnCheckListener {
        fun onCheck(data: List<WithdrawItem?>?, position: Int)
    }
}
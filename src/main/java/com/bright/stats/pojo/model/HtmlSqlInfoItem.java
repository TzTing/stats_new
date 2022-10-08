package com.bright.stats.pojo.model;

import com.bright.stats.pojo.po.primary.SqlInfoItem;

public class HtmlSqlInfoItem extends SqlInfoItem {
	private static final long serialVersionUID = 1142735172085032829L;
	private String title;
	private Integer rowspan; //占几行
	private Integer colspan; //占几列
	private boolean sortable; //排序
	private Integer align; //对齐
	private boolean noShow;
	private boolean last;
	private boolean needRemove;
	private Integer count;
	private boolean isLastColumn;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getRowspan() {
		return rowspan;
	}
	public void setRowspan(Integer rowspan) {
		this.rowspan = rowspan;
	}
	public Integer getColspan() {
		return colspan;
	}
	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}
	public boolean isSortable() {
		return sortable;
	}
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
	public Integer getAlign() {
		return align;
	}
	public void setAlign(Integer align) {
		this.align = align;
	}
	public boolean isNoShow() {
		return noShow;
	}
	public void setNoShow(boolean noShow) {
		this.noShow = noShow;
	}
	public boolean isLast() {
		return last;
	}
	public void setLast(boolean last) {
		this.last = last;
	}
	public boolean isNeedRemove() {
		return needRemove;
	}
	public void setNeedRemove(boolean needRemove) {
		this.needRemove = needRemove;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public boolean isLastColumn() {
		return isLastColumn;
	}
	public void setLastColumn(boolean isLastColumn) {
		this.isLastColumn = isLastColumn;
	}
 
	
	
	
	

}

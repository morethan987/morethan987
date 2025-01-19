import tkinter as tk
from tkinter import ttk, filedialog, messagebox
import os
from main import load_mat_file, Workflow, SelectionRule, save_to_excel
import numpy as np
import pandas as pd  # 添加这行导入

class DataExtractorGUI:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("数据提取器")
        self.root.geometry("800x600")
        
        self.workflow = None
        self.current_rules = []
        
        self._create_widgets()
        self._layout_widgets()
        
    def _create_widgets(self):
        # 文件选择部分
        self.file_frame = ttk.LabelFrame(self.root, text="文件选择", padding=10)
        self.file_path = tk.StringVar()
        self.file_entry = ttk.Entry(self.file_frame, textvariable=self.file_path)
        self.browse_btn = ttk.Button(self.file_frame, text="浏览", command=self._browse_file)
        self.var_name = tk.StringVar()
        self.var_entry = ttk.Entry(self.file_frame, textvariable=self.var_name)
        self.load_btn = ttk.Button(self.file_frame, text="加载数据", command=self._load_data)
        
        # 规则输入部分
        self.rule_frame = ttk.LabelFrame(self.root, text="规则配置", padding=10)
        self.row_rule = tk.StringVar()
        self.col_rule = tk.StringVar()
        self.row_entry = ttk.Entry(self.rule_frame, textvariable=self.row_rule)
        self.col_entry = ttk.Entry(self.rule_frame, textvariable=self.col_rule)
        self.add_rule_btn = ttk.Button(self.rule_frame, text="添加规则", command=self._add_rule)
        self.help_btn = ttk.Button(self.rule_frame, text="规则说明", command=self._show_help)
        
        # 规则列表
        self.rules_list = tk.Listbox(self.rule_frame, height=5)
        self.remove_rule_btn = ttk.Button(self.rule_frame, text="删除规则", command=self._remove_rule)
        
        # 预览和导出部分
        self.preview_frame = ttk.LabelFrame(self.root, text="预览和导出", padding=10)
        self.preview_text = tk.Text(self.preview_frame, height=10)
        self.export_btn = ttk.Button(self.preview_frame, text="导出到Excel", command=self._export_data)
        
    def _layout_widgets(self):
        # 文件选择布局
        self.file_frame.pack(fill=tk.X, padx=5, pady=5)
        self.file_entry.pack(side=tk.LEFT, expand=True, fill=tk.X, padx=5)
        self.browse_btn.pack(side=tk.LEFT, padx=5)
        self.var_entry.pack(side=tk.LEFT, padx=5)
        self.load_btn.pack(side=tk.LEFT, padx=5)
        
        # 规则配置布局
        self.rule_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        ttk.Label(self.rule_frame, text="行规则:").pack(anchor=tk.W)
        self.row_entry.pack(fill=tk.X, padx=5)
        ttk.Label(self.rule_frame, text="列规则:").pack(anchor=tk.W)
        self.col_entry.pack(fill=tk.X, padx=5)
        
        btn_frame = ttk.Frame(self.rule_frame)
        btn_frame.pack(fill=tk.X, pady=5)
        self.add_rule_btn.pack(side=tk.LEFT, padx=5)
        self.help_btn.pack(side=tk.LEFT, padx=5)
        self.remove_rule_btn.pack(side=tk.LEFT, padx=5)
        
        self.rules_list.pack(fill=tk.BOTH, expand=True, padx=5)
        
        # 预览和导出布局
        self.preview_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        self.preview_text.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        self.export_btn.pack(pady=5)
        
    def _browse_file(self):
        filename = filedialog.askopenfilename(
            title="选择MAT文件",
            filetypes=[("MAT files", "*.mat"), ("All files", "*.*")],
            initialdir="./data"
        )
        if filename:
            self.file_path.set(filename)
            self.var_name.set(os.path.splitext(os.path.basename(filename))[0])
            
    def _load_data(self):
        try:
            filename = self.file_path.get()
            if not filename:
                messagebox.showerror("错误", "请选择文件")
                return
            
            # 直接使用完整路径    
            mat_data = load_mat_file(filename)
            var_name = self.var_name.get()
            if not var_name:
                messagebox.showerror("错误", "请输入变量名")
                return
                
            try:
                self.workflow = Workflow(mat_data[var_name])
                messagebox.showinfo("成功", f"数据加载成功\n形状: {self.workflow.data.shape}")
            except KeyError:
                messagebox.showerror("错误", f"在文件中未找到变量 '{var_name}'")
        except Exception as e:
            messagebox.showerror("错误", f"加载文件失败: {str(e)}")
            
    def _parse_rule(self, rule_str):
        if ':' in rule_str:
            parts = rule_str.split(':')
            return SelectionRule.by_range(
                int(parts[0]) if parts[0] else 0,
                int(parts[1]) if parts[1] else None,
                int(parts[2]) if len(parts) > 2 and parts[2] else 1
            )
        elif ',' in rule_str:
            return SelectionRule.by_indices([int(x.strip()) for x in rule_str.split(',')])
        else:
            return SelectionRule.by_single(int(rule_str))
            
    def _add_rule(self):
        try:
            if not self.workflow:
                messagebox.showerror("错误", "请先加载数据")
                return
                
            row_rule = self._parse_rule(self.row_rule.get())
            col_rule = self._parse_rule(self.col_rule.get())
            
            self.workflow.add_extraction(row_rule, col_rule)
            rule_text = f"行: {self.row_rule.get()} | 列: {self.col_rule.get()}"
            self.rules_list.insert(tk.END, rule_text)
            self.current_rules.append((row_rule, col_rule))
            
            # 更新预览
            self._update_preview()
        except Exception as e:
            messagebox.showerror("错误", str(e))
            
    def _remove_rule(self):
        selection = self.rules_list.curselection()
        if not selection:
            return
            
        index = selection[0]
        self.rules_list.delete(index)
        self.current_rules.pop(index)
        
        # 重新创建workflow并应用剩余规则
        if self.workflow:
            data = self.workflow.data
            self.workflow = Workflow(data)
            for row_rule, col_rule in self.current_rules:
                self.workflow.add_extraction(row_rule, col_rule)
            self._update_preview()
            
    def _update_preview(self):
        if not self.workflow:
            return
            
        merged_data = self.workflow.get_merged_data()
        preview = f"数据形状: {merged_data.shape}\n"
        preview += f"非空值数量: {np.count_nonzero(~np.isnan(merged_data))}\n"
        preview += "\n预览前10行10列:\n"
        preview += str(pd.DataFrame(merged_data[:10, :10]))
        
        self.preview_text.delete(1.0, tk.END)
        self.preview_text.insert(1.0, preview)
        
    def _export_data(self):
        if not self.workflow:
            messagebox.showerror("错误", "没有数据可导出")
            return
            
        filename = filedialog.asksaveasfilename(
            title="保存Excel文件",
            filetypes=[("Excel files", "*.xlsx"), ("All files", "*.*")],
            defaultextension=".xlsx"
        )
        
        if filename:
            try:
                merged_data = self.workflow.get_merged_data()
                save_to_excel(merged_data, os.path.basename(filename))
                messagebox.showinfo("成功", "数据已成功导出")
            except Exception as e:
                messagebox.showerror("错误", str(e))
                
    def _show_help(self):
        help_text = """规则输入说明：
1. 单个索引: 输入一个数字，如 '5' 或 '-1'
2. 索引列表: 输入以逗号分隔的数字，如 '1,3,5'
3. 范围规则: 输入三个以冒号分隔的数字(起始:结束:步长)，如 '0:-1:60'"""
        messagebox.showinfo("规则说明", help_text)
        
    def run(self):
        self.root.mainloop()

if __name__ == "__main__":
    app = DataExtractorGUI()
    app.run()

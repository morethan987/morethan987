from toolbox import update_ui, promote_file_to_downloadzone
from toolbox import CatchException, report_exception, write_history_to_file
from shared_utils.fastapi_server import validate_path_safety
from crazy_functions.crazy_utils import input_clipping

def 生成论文级算法描述(txt, llm_kwargs, plugin_kwargs, chatbot, history, system_prompt, user_request):
    history = []    # 清空历史，以免输入溢出
    import glob, os
    if os.path.exists(txt):
        project_folder = txt
        validate_path_safety(project_folder, chatbot.get_user())
    else:
        if txt == "": txt = '空空如也的输入栏'
        report_exception(chatbot, history, a = f"解析Matlab项目: {txt}", b = f"找不到本地项目或无权访问: {txt}")
        yield from update_ui(chatbot=chatbot, history=history) # 刷新界面
        return
    file_manifest = [f for f in glob.glob(f'{project_folder}/**/*.m', recursive=True)]
    if len(file_manifest) == 0:
        report_exception(chatbot, history, a = f"解析Matlab项目: {txt}", b = f"找不到任何`.m`源文件: {txt}")
        yield from update_ui(chatbot=chatbot, history=history) # 刷新界面
        return
    
    import os, copy
    from crazy_functions.crazy_utils import request_gpt_model_multi_threads_with_very_awesome_ui_and_high_efficiency
    from crazy_functions.crazy_utils import request_gpt_model_in_new_thread_with_ui_alive

    summary_batch_isolation = True
    inputs_array = []
    inputs_show_user_array = []
    history_array = []
    sys_prompt_array = []
    report_part_1 = []

    assert len(file_manifest) <= 512, "源文件太多（超过512个）, 请缩减输入文件的数量。或者，您也可以选择删除此行警告，并修改代码拆分file_manifest列表，从而实现分批次处理。"
    ############################## <第一步，逐个文件分析，多线程> ##################################
    for index, fp in enumerate(file_manifest):
        # 读取文件
        with open(fp, 'r', encoding='utf-8', errors='replace') as f:
            file_content = f.read()
        prefix = "接下来请你逐文件分析下面的工程" if index==0 else ""
        i_say = prefix + f'请对下面的程序文件做一个概述文件名是{os.path.relpath(fp, project_folder)}，文件代码是 ```{file_content}```'
        i_say_show_user = prefix + f'[{index+1}/{len(file_manifest)}] 请对下面的程序文件做一个概述: {fp}'
        # 装载请求内容
        inputs_array.append(i_say)
        inputs_show_user_array.append(i_say_show_user)
        history_array.append([])
        sys_prompt_array.append("你是一个程序架构分析师，正在分析一个源代码项目。你的回答必须简单明了。")

    # 文件读取完成，对每一个源代码文件，生成一个请求线程，发送到chatgpt进行分析
    gpt_response_collection = yield from request_gpt_model_multi_threads_with_very_awesome_ui_and_high_efficiency(
        inputs_array = inputs_array,
        inputs_show_user_array = inputs_show_user_array,
        history_array = history_array,
        sys_prompt_array = sys_prompt_array,
        llm_kwargs = llm_kwargs,
        chatbot = chatbot,
        show_user_at_complete = True
    )

    # 全部文件解析完成，结果写入文件，准备对工程源代码进行汇总分析
    report_part_1 = copy.deepcopy(gpt_response_collection)
    history_to_return = report_part_1
    res = write_history_to_file(report_part_1)
    promote_file_to_downloadzone(res, chatbot=chatbot)
    chatbot.append(("完成？", "逐个文件分析已完成。" + res + "\n\n正在开始汇总。"))
    yield from update_ui(chatbot=chatbot, history=history_to_return) # 刷新界面

############################## <第二步，综合，单线程，分组+迭代处理> ##################################
    batchsize = 16  # 16个文件为一组
    report_part_2 = []
    previous_iteration_files = []
    last_iteration_result = ""
    while True:
        if len(file_manifest) == 0: break
        this_iteration_file_manifest = file_manifest[:batchsize]
        this_iteration_gpt_response_collection = gpt_response_collection[:batchsize*2]
        file_rel_path = [os.path.relpath(fp, project_folder) for index, fp in enumerate(this_iteration_file_manifest)]
        # 把“请对下面的程序文件做一个概述” 替换成 精简的 "文件名：{all_file[index]}"
        for index, content in enumerate(this_iteration_gpt_response_collection):
            if index%2==0: this_iteration_gpt_response_collection[index] = f"请对下面的程序文件做一个概述：{file_rel_path[index//2]}" # 只保留文件名节省token
        this_iteration_files = [os.path.relpath(fp, project_folder) for index, fp in enumerate(this_iteration_file_manifest)]
        previous_iteration_files.extend(this_iteration_files)
        previous_iteration_files_string = ', '.join(previous_iteration_files)
        current_iteration_focus = ', '.join(this_iteration_files)
        if summary_batch_isolation: focus = current_iteration_focus
        else:                       focus = previous_iteration_files_string
        i_say = f'用一张Markdown表格简要描述以下文件的功能：{focus}。根据以上分析，用一句话概括程序的整体功能。'
        if last_iteration_result != "":
            sys_prompt_additional = "已知某些代码的局部作用是:" + last_iteration_result + "\n请继续分析其他源代码，从而更全面地理解项目的整体功能。"
        else:
            sys_prompt_additional = ""
        inputs_show_user = f'根据以上分析，对程序的整体功能和构架重新做出概括，由于输入长度限制，可能需要分组处理，本组文件为 {current_iteration_focus} + 已经汇总的文件组。'
        this_iteration_history = copy.deepcopy(this_iteration_gpt_response_collection)
        this_iteration_history.append(last_iteration_result)
        # 裁剪input
        inputs, this_iteration_history_feed = input_clipping(inputs=i_say, history=this_iteration_history, max_token_limit=2560)
        result = yield from request_gpt_model_in_new_thread_with_ui_alive(
            inputs=inputs, inputs_show_user=inputs_show_user, llm_kwargs=llm_kwargs, chatbot=chatbot,
            history=this_iteration_history_feed,   # 迭代之前的分析
            sys_prompt="你是一个程序架构分析师，正在分析一个项目的源代码。" + sys_prompt_additional)

        diagram_code = make_diagram(this_iteration_files, result, this_iteration_history_feed)
        summary = "请分步描述这些文件所代表的整体算法描述，要求语言流畅逻辑清晰，术语表达准确，对于算法的描述详尽而不冗杂，重点强调那些具有关键作用或者创新性的算法步骤，忽略参数初始化等非重要步骤。但是请不要直接引用文件名，不要输出代码。输出按照以下格式生成：\nStep1：步骤名称\n步骤一的描述\nStep2：步骤名称\n步骤二的描述\n如果有更多的步骤可以按照格式自行添加，但最多不要超过四个\n\n" + diagram_code
        summary_result = yield from request_gpt_model_in_new_thread_with_ui_alive(
            inputs=summary,
            inputs_show_user=summary,
            llm_kwargs=llm_kwargs,
            chatbot=chatbot,
            history=[i_say, result],   # 迭代之前的分析
            sys_prompt="你是一个程序架构分析师，正在分析一个项目的源代码。在你分析完成之后，你需要按照特定的格式生成一份论文级的项目描述" + sys_prompt_additional)

        report_part_2.extend([i_say, result])
        last_iteration_result = summary_result
        file_manifest = file_manifest[batchsize:]
        gpt_response_collection = gpt_response_collection[batchsize*2:]

    ############################## <END> ##################################
    history_to_return.extend(report_part_2)
    res = write_history_to_file(history_to_return)
    promote_file_to_downloadzone(res, chatbot=chatbot)
    chatbot.append(("完成了吗？", res))
    yield from update_ui(chatbot=chatbot, history=history_to_return) # 刷新界面

def make_diagram(this_iteration_files, result, this_iteration_history_feed):
    from crazy_functions.diagram_fns.file_tree import build_file_tree_mermaid_diagram
    return build_file_tree_mermaid_diagram(this_iteration_history_feed[0::2], this_iteration_history_feed[1::2], "项目示意图")
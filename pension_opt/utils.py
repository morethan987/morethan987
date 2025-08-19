from datetime import datetime


def get_year_diff(start, N):
    start_date = datetime.strptime(start, "%Y%m")
    total_months = start_date.year * 12 + (start_date.month - 1) + N
    end_year = total_months // 12
    return end_year - start_date.year + 1


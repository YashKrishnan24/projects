"""
╔══════════════════════════════════════════╗
║       GUESS THE DAY  🗓️  Calendar Game    ║
╚══════════════════════════════════════════╝

A fun terminal game where you guess which day
of the week a given date falls on.

Requirements:
    pip install rich
"""

import random
import calendar
from datetime import date, timedelta
from rich.console import Console
from rich.panel import Panel
from rich.text import Text
from rich.table import Table
from rich.prompt import Prompt
from rich.align import Align
from rich import box
from rich.columns import Columns
from rich.rule import Rule
from rich.live import Live
import time

console = Console()

DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
DAY_EMOJIS = {
    "Monday":    "🌅",
    "Tuesday":   "🔥",
    "Wednesday": "⚡",
    "Thursday":  "🌿",
    "Friday":    "🎉",
    "Saturday":  "🌟",
    "Sunday":    "☀️",
}
DAY_COLORS = {
    "Monday":    "steel_blue1",
    "Tuesday":   "orange_red1",
    "Wednesday": "gold1",
    "Thursday":  "medium_spring_green",
    "Friday":    "hot_pink",
    "Saturday":  "medium_purple1",
    "Sunday":    "bright_yellow",
}

MONTH_NAMES = [
    "", "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
]

MONTH_EMOJIS = [
    "", "❄️", "💘", "🌸", "🌧️", "🌻", "☀️",
    "🏖️", "🍦", "🍂", "🎃", "❄️", "🎄"
]

DIFFICULTY = {
    "easy":   {"range_years": 2,  "hints": True,  "label": "Easy 🌱",   "color": "green"},
    "medium": {"range_years": 10, "hints": False, "label": "Medium 🔥", "color": "yellow"},
    "hard":   {"range_years": 50, "hints": False, "label": "Hard 💀",   "color": "red"},
}


def clear():
    console.clear()


def splash_screen():
    clear()
    title = Text()
    title.append("  ╔══════════════════════════════════════╗\n", style="bold cyan")
    title.append("  ║  ", style="bold cyan")
    title.append("🗓️  GUESS THE DAY ", style="bold white")
    title.append("Calendar Quiz  ", style="bold yellow")
    title.append("║\n", style="bold cyan")
    title.append("  ╚══════════════════════════════════════╝", style="bold cyan")

    console.print()
    console.print(Align.center(title))
    console.print()
    console.print(Align.center(
        Text("Test your calendar knowledge — what day did that date fall on?",
             style="italic dim white")
    ))
    console.print()

    # Animated dots
    with console.status("[bold cyan]Loading...", spinner="dots12"):
        time.sleep(1.2)


def show_stats_panel(score: dict):
    table = Table(box=box.SIMPLE, show_header=False, padding=(0, 2))
    table.add_column(style="dim white")
    table.add_column(style="bold white")

    table.add_row("✅ Correct",  f"[green]{score['correct']}[/green]")
    table.add_row("❌ Wrong",    f"[red]{score['wrong']}[/red]")
    table.add_row("🎯 Accuracy",
                  f"[cyan]{score['correct'] / max(score['total'], 1) * 100:.0f}%[/cyan]")
    table.add_row("🔢 Streak",   f"[yellow]{score['streak']}🔥[/yellow]"
                  if score["streak"] > 1 else f"[white]{score['streak']}[/white]")

    return Panel(table, title="[bold]📊 Stats[/bold]",
                 border_style="cyan", padding=(0, 1))


def show_calendar_hint(target: date):
    """Show a mini calendar for the target month as a hint."""
    cal = calendar.monthcalendar(target.year, target.month)
    grid = Table(box=box.SIMPLE_HEAD, show_header=True, padding=(0, 1))
    for d in ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"]:
        style = "bold magenta" if d in ("Sa", "Su") else "bold cyan"
        grid.add_column(d, style=style, justify="right", width=3)

    for week in cal:
        row = []
        for i, day_num in enumerate(week):
            if day_num == 0:
                row.append("  ")
            elif day_num == target.day:
                row.append(f"[bold yellow on blue] {day_num} [/]")
            elif i >= 5:
                row.append(f"[magenta]{day_num}[/]")
            else:
                row.append(str(day_num))
        grid.add_row(*row)

    header = f"{MONTH_EMOJIS[target.month]} {MONTH_NAMES[target.month]} {target.year}"
    console.print(Panel(Align.center(grid), title=f"[bold]{header}[/bold]",
                        border_style="dim cyan", padding=(0, 2)))


def pick_random_date(difficulty: str) -> date:
    yr = DIFFICULTY[difficulty]["range_years"]
    today = date.today()
    start = today - timedelta(days=yr * 365)
    end   = today + timedelta(days=yr * 365)
    delta = (end - start).days
    return start + timedelta(days=random.randint(0, delta))


def draw_day_buttons(highlight: str = None):
    """Draw 7 day option buttons."""
    panels = []
    for i, day in enumerate(DAYS, 1):
        color = DAY_COLORS[day]
        emoji = DAY_EMOJIS[day]
        if highlight == day:
            style = f"bold white on {color}"
        else:
            style = f"bold {color}"
        text = Text(f" {i}. {emoji} {day} ", style=style, justify="center")
        panels.append(Panel(text, border_style=color, padding=(0, 1)))
    console.print(Columns(panels, equal=True, expand=True))


def get_user_guess() -> str:
    console.print()
    console.print(Rule("[dim]Enter the number or name of the day[/dim]", style="dim"))
    console.print()
    while True:
        raw = Prompt.ask("[bold cyan]  ➜  Your guess[/bold cyan]").strip()
        # Accept number 1-7
        if raw.isdigit() and 1 <= int(raw) <= 7:
            return DAYS[int(raw) - 1]
        # Accept day name (case-insensitive, partial)
        for day in DAYS:
            if day.lower().startswith(raw.lower()) and len(raw) >= 2:
                return day
        console.print("  [red]⚠  Please enter a number 1–7 or a day name.[/red]")


def show_result(correct: bool, guess: str, answer: str, target: date):
    console.print()
    if correct:
        color = DAY_COLORS[answer]
        emoji = DAY_EMOJIS[answer]
        msg = Text()
        msg.append(f"\n  🎉 CORRECT!  ", style=f"bold white on {color}")
        msg.append(f"  {emoji} {answer}  ", style=f"bold {color}")
        console.print(Panel(msg, border_style=color, padding=(0, 2)))
    else:
        a_color = DAY_COLORS[answer]
        g_color = DAY_COLORS[guess]
        msg = Text()
        msg.append(f"\n  ❌  Not quite!  ", style="bold white on red")
        msg.append(f"\n\n  You guessed:  ", style="dim white")
        msg.append(f"{DAY_EMOJIS[guess]} {guess}", style=f"bold {g_color}")
        msg.append(f"\n  Correct day:  ", style="dim white")
        msg.append(f"{DAY_EMOJIS[answer]} {answer}\n", style=f"bold {a_color}")
        console.print(Panel(msg, border_style="red", padding=(0, 2)))

    # Fun fact
    iso_wd = target.isoweekday()  # 1=Mon … 7=Sun
    week_num = target.isocalendar()[1]
    console.print(
        f"  [dim]📅  {target.strftime('%d %B %Y')} was week [bold]{week_num}[/] "
        f"of {target.year}, day {iso_wd} of the week.[/dim]"
    )
    console.print()


def choose_difficulty() -> str:
    console.print()
    console.print(Rule("[bold]Choose Difficulty[/bold]", style="cyan"))
    console.print()
    for key, val in DIFFICULTY.items():
        yr = val["range_years"]
        hints = "with hints" if val["hints"] else "no hints"
        console.print(
            f"  [{val['color']}][bold]{key[0].upper()}[/bold][/{val['color']}]"
            f" → [bold]{val['label']}[/bold]"
            f"  [dim](±{yr} year{'s' if yr > 1 else ''}, {hints})[/dim]"
        )
    console.print()
    while True:
        choice = Prompt.ask(
            "[bold cyan]  Pick difficulty[/bold cyan]",
            choices=["e", "m", "h", "easy", "medium", "hard"],
            default="easy"
        ).lower()
        for key in DIFFICULTY:
            if key.startswith(choice):
                return key


def show_final_summary(score: dict, difficulty: str):
    clear()
    console.print()
    console.print(Rule("[bold yellow]🏁  Game Over[/bold yellow]", style="yellow"))
    console.print()

    pct = score["correct"] / max(score["total"], 1) * 100
    if pct == 100:
        verdict, style = "🏆 PERFECT SCORE!", "bold gold1"
    elif pct >= 70:
        verdict, style = "🎯 Great job!", "bold green"
    elif pct >= 40:
        verdict, style = "👍 Not bad!", "bold yellow"
    else:
        verdict, style = "📚 Keep practising!", "bold red"

    table = Table(box=box.DOUBLE_EDGE, show_header=False,
                  border_style="cyan", padding=(0, 3))
    table.add_column(style="dim white", justify="right")
    table.add_column(style="bold white")

    table.add_row("Difficulty",   DIFFICULTY[difficulty]["label"])
    table.add_row("Questions",    str(score["total"]))
    table.add_row("Correct ✅",   f"[green]{score['correct']}[/green]")
    table.add_row("Wrong ❌",     f"[red]{score['wrong']}[/red]")
    table.add_row("Accuracy 🎯",  f"[cyan]{pct:.0f}%[/cyan]")
    table.add_row("Best Streak 🔥", f"[yellow]{score['best_streak']}[/yellow]")
    table.add_row("Verdict",      f"[{style}]{verdict}[/{style}]")

    console.print(Align.center(table))
    console.print()


def play(difficulty: str, rounds: int = 10):
    score = {"correct": 0, "wrong": 0, "total": 0,
             "streak": 0, "best_streak": 0}

    for round_num in range(1, rounds + 1):
        clear()
        target = pick_random_date(difficulty)
        answer = DAYS[target.weekday()]  # weekday(): 0=Mon … 6=Sun

        # Header
        console.print()
        console.print(Rule(
            f"[bold cyan]Round {round_num} / {rounds}[/bold cyan]",
            style="cyan"
        ))
        console.print()

        # Stats sidebar + date display
        stats_panel = show_stats_panel(score)
        month_str   = f"{MONTH_EMOJIS[target.month]}  {MONTH_NAMES[target.month]}"
        date_text   = Text(justify="center")
        date_text.append(f"\n{month_str}\n", style="bold dim white")
        date_text.append(f"{target.day}", style="bold white", )
        date_text.stylize("font-size:72px")  # no-op in terminal, just semantic
        date_text.append(f"\n{target.year}\n", style="bold white")

        day_panel = Panel(
            Align.center(
                Text.assemble(
                    ("\n", ""),
                    (f"  {month_str}  \n\n", "bold dim white"),
                    (f"  {target.day}  ", "bold white"),
                    ("\n\n", ""),
                    (f"  {target.year}  \n", "bold dim white"),
                )
            ),
            title="[bold yellow]📅  What day is this?[/bold yellow]",
            border_style="yellow",
            padding=(1, 6),
        )
        console.print(Columns([stats_panel, day_panel], equal=False, expand=True))

        # Optional hint
        if DIFFICULTY[difficulty]["hints"]:
            console.print()
            show_calendar_hint(target)
        else:
            console.print()
            console.print("  [dim](No calendar hint in this difficulty)[/dim]")
            console.print()

        # Day buttons
        draw_day_buttons()

        # Get guess
        guess = get_user_guess()
        correct = guess == answer

        # Update score
        score["total"] += 1
        if correct:
            score["correct"] += 1
            score["streak"]  += 1
            score["best_streak"] = max(score["best_streak"], score["streak"])
        else:
            score["wrong"]  += 1
            score["streak"] = 0

        # Show result
        show_result(correct, guess, answer, target)

        if round_num < rounds:
            Prompt.ask("[dim]  Press Enter for next question[/dim]", default="")

    return score


def main():
    splash_screen()

    while True:
        difficulty = choose_difficulty()
        console.print()
        rounds_str = Prompt.ask(
            "[bold cyan]  How many questions?[/bold cyan]",
            default="10"
        )
        try:
            rounds = max(1, min(50, int(rounds_str)))
        except ValueError:
            rounds = 10

        score = play(difficulty, rounds)
        show_final_summary(score, difficulty)

        again = Prompt.ask(
            "[bold cyan]  Play again?[/bold cyan]",
            choices=["y", "n"],
            default="y"
        )
        if again.lower() != "y":
            console.print()
            console.print(Align.center(
                Text("Thanks for playing! 🗓️  See you next time!", style="bold cyan")
            ))
            console.print()
            break


if __name__ == "__main__":
    main()

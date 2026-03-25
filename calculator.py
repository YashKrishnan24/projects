import tkinter as tk

root = tk.Tk()
root.title("Calculator")
root.geometry("320x450")
root.configure(bg="#1e1e1e")

entry = tk.Entry(root, font=("Segoe UI", 24), bd=0, bg="#1e1e1e", fg="white", justify="right")
entry.pack(fill="both", ipadx=8, ipady=20, padx=10, pady=10)

def press(x):
    entry.insert(tk.END, x)

def clear():
    entry.delete(0, tk.END)

def equal():
    try:
        entry.delete(0, tk.END)
        entry.insert(0, str(eval(expr)))
    except:
        entry.delete(0, tk.END)
        entry.insert(0, "Error")

def calculate():
    global expr
    expr = entry.get()
    equal()

buttons = [
    ("C", "#ff5c5c"), ("(", "#3a3a3a"), (")", "#3a3a3a"), ("/", "#ff9500"),
    ("7", "#2d2d2d"), ("8", "#2d2d2d"), ("9", "#2d2d2d"), ("*", "#ff9500"),
    ("4", "#2d2d2d"), ("5", "#2d2d2d"), ("6", "#2d2d2d"), ("-", "#ff9500"),
    ("1", "#2d2d2d"), ("2", "#2d2d2d"), ("3", "#2d2d2d"), ("+", "#ff9500"),
    ("0", "#2d2d2d"), (".", "#2d2d2d"), ("=", "#00c853")
]

frame = tk.Frame(root, bg="#1e1e1e")
frame.pack(expand=True, fill="both")

row = 0
col = 0

for (text, color) in buttons:
    action = calculate if text == "=" else clear if text == "C" else lambda t=text: press(t)
    btn = tk.Button(frame, text=text, bg=color, fg="white", font=("Segoe UI", 16), bd=0, command=action)
    btn.grid(row=row, column=col, sticky="nsew", padx=5, pady=5)
    col += 1
    if col > 3:
        col = 0
        row += 1

for i in range(5):
    frame.rowconfigure(i, weight=1)
for i in range(4):
    frame.columnconfigure(i, weight=1)

root.mainloop()

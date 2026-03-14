import turtle
import math
import random

def setup_cosmic_pet():
    # Screen Setup - Dark Mode
    screen = turtle.Screen()
    screen.title("Cosmic Turtle Follower")
    screen.bgcolor("#0b0e14") # Deep space midnight blue
    screen.setup(width=800, height=800)
    screen.tracer(0)

    # The Pet
    pet = turtle.Turtle()
    pet.shape("turtle")
    pet.color("cyan")
    pet.penup()
    pet.speed(0)
    pet.pensize(3)
    
    # State variables
    colors = ["#ff0055", "#00ffcc", "#0088ff", "#cc00ff", "#ffff00"]
    particles = []

    def create_ripple(x, y):
        """Creates a greeting and a visual pulse."""
        # Visual Greeting
        writer = turtle.Turtle()
        writer.hideturtle()
        writer.penup()
        writer.color("white")
        writer.goto(x, y + 30)
        writer.write("✨ Hello there! ✨", align="center", font=("Courier", 16, "bold italic"))
        
        # Flash effect
        screen.bgcolor("#1a1a2e")
        screen.ontimer(lambda: screen.bgcolor("#0b0e14"), 100)
        screen.ontimer(writer.clear, 1200)

    def spawn_particle(x, y):
        """Creates a small star that fades."""
        p = turtle.Turtle()
        p.hideturtle()
        p.shape("circle")
        p.shapesize(0.2)
        p.color(random.choice(colors))
        p.penup()
        p.goto(x, y)
        p.showturtle()
        particles.append([p, 1.0]) # [turtle_object, opacity/life]

    def update_particles():
        """Animates and removes old particles."""
        for p_data in particles[:]:
            p_obj, life = p_data
            p_data[1] -= 0.05 # Reduce life
            if p_data[1] <= 0:
                p_obj.hideturtle()
                particles.remove(p_data)
            else:
                p_obj.shapesize(p_data[1] * 0.5)

    # Click Event
    pet.onclick(create_ripple)

    def main_loop():
        # 1. Track Mouse
        canvas = screen.getcanvas()
        mx, my = canvas.winfo_pointerx() - canvas.winfo_rootx(), \
                 canvas.winfo_pointery() - canvas.winfo_rooty()
        
        target_x = mx - screen.window_width() / 2
        target_y = (screen.window_height() / 2) - my

        # 2. Physics & Calculation
        dx = target_x - pet.xcor()
        dy = target_y - pet.ycor()
        distance = math.sqrt(dx**2 + dy**2)

        # Move and Rotate
        pet.goto(pet.xcor() + dx * 0.1, pet.ycor() + dy * 0.1)
        if distance > 1:
            pet.setheading(pet.towards(target_x, target_y))

        # 3. Creative Effects
        # Dynamic Color based on position
        hue = (pet.xcor() + pet.ycor()) % 360
        pet.color(colors[int((hue/360) * len(colors))])
        
        # Dynamic Size (Grows when moving fast)
        size_factor = 1 + (min(distance, 100) / 50)
        pet.shapesize(size_factor, size_factor)

        # Trail & Particles
        if distance > 5:
            pet.pendown()
            if random.random() > 0.8: # Random chance to drop a sparkle
                spawn_particle(pet.xcor(), pet.ycor())
        else:
            pet.penup()

        update_particles()
        screen.update()
        screen.ontimer(main_loop, 15)

    main_loop()
    screen.mainloop()

if __name__ == "__main__":
    setup_cosmic_pet()

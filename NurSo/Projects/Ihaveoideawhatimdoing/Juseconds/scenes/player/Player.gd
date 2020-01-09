extends KinematicBody2D

const GRAVITY = 20
const ACCELERATION = 32
const MAX_SPEED = 200
const JUMP_HEIGHT = 500
const GROUND_FRICTION = 0.3
const AIR_FRICTION = 0.05

var hp = 100

var velocity = Vector2()

func _ready():
	add_to_group("player")
	hp = 100

func _physics_process(delta):
	velocity.y += GRAVITY
	var b_friction = false
	
	if Input.is_action_pressed("ui_left"):
		velocity.x = max(velocity.x - ACCELERATION, -MAX_SPEED)
		$AnimatedSprite.flip_h = true
		$AnimatedSprite.play("walking")
	elif Input.is_action_pressed("ui_right"):
		velocity.x = min(velocity.x + ACCELERATION, MAX_SPEED)
		$AnimatedSprite.flip_h = false
		$AnimatedSprite.play("walking")
	else:
		b_friction = true
		$AnimatedSprite.play("idle")
		
	if is_on_floor():
		if Input.is_action_just_pressed("ui_up"):
			velocity.y = -JUMP_HEIGHT
		if b_friction:
			velocity.x = lerp(velocity.x, 0, GROUND_FRICTION)
	else:
		if b_friction:
			velocity.x = lerp(velocity.x, 0, AIR_FRICTION)
		
	velocity = move_and_slide(velocity, Vector2(0, -1))
	
func take_damage(value):
	if $HUD:
		hp -= value
		$HUD.update_hp(int(round(hp)))
		if hp <= 0:
			play_death()

func play_death():
	if $HUD:
		$HUD.show_message("You dies!")
extends CanvasLayer

export var b_show_fps = true

func show_message(text):
	$Message.text = text
	$AnimationPlayer.play("show_message")
	
func hide():
	$HUD_Box.hide()

func show():
	$HUD_Box.show()
	
func update_hp(value):
	$HUD_Box/HBoxContainer/HP.text = str(value)

# Called when the node enters the scene tree for the first time.
func _ready():
	pass # Replace with function body.

# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta):
	if b_show_fps and delta != 0:
		$HUD_Box/HBoxContainer/FPS.text = str(int(round(1/delta)))

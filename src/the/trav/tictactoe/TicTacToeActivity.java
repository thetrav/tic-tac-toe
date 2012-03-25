package the.trav.tictactoe;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class TicTacToeActivity extends BaseGameActivity {

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 720;
	
	private static int idx = 0;
	private static final int NAUGHT = idx++;
	private static final int CROSS = idx++;
	private static final int BOARD = idx++;
	private static final int RESET = idx++;
	private static final int LINE_HORIZONTAL = idx++;
	private static final int LINE_VERTICAL = idx++;
	private static final int LINE_TOP_LEFT = idx++;
	private static final int LINE_BOTTOM_LEFT = idx++;
	
	private final int[][] coords = {
		{5  , 98, 189 }, //X's
        {7 , 98, 188 }   //Y's
	};
	private final int X = 0;
	private final int Y = 1;
	
	private Camera mCamera;
	
	private TextureRegion[] textures =  new TextureRegion[idx];
	
	private boolean gameOver = false;
	private int currentPlayer = CROSS;
	private Scene scene;
	
	private Sprite playerSprite = null;
	
	private int [][] boardState = new int[3][3];

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}

	private TextureRegion loadTexture(final int atlasWidth, final int atlasHeight, final String resourceName) {
		final BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas(atlasWidth, atlasHeight, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		final TextureRegion region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, resourceName, 0, 0);
		this.mEngine.getTextureManager().loadTexture(bitmapTextureAtlas);
		return region;
	}
	
	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		textures[NAUGHT] = loadTexture(128, 128, "naught.png");
		textures[CROSS] = loadTexture(128, 128, "cross.png");
		textures[BOARD] = loadTexture(512, 512, "board.png");
		textures[RESET] = loadTexture(512, 512, "menu_reset.png");
		textures[LINE_HORIZONTAL] = loadTexture(256, 256, "horizontal_line.png");
		textures[LINE_VERTICAL] = loadTexture(256, 256, "vertical_line.png");
		textures[LINE_TOP_LEFT] = loadTexture(256, 256, "diagonal_line_from_top.png");
		textures[LINE_BOTTOM_LEFT] = loadTexture(256, 256, "diagonal_line_from_bottom.png");
	}

	private Sprite addSprite(Entity parent, int x, int y, TextureRegion texture) {
		final Sprite sprite = new Sprite(x, y, texture);
		parent.attachChild(sprite);
		return sprite;
	}
	
	private int centerX(int texture) {
		return (CAMERA_WIDTH - textures[texture].getWidth()) / 2;
	}
	
	private int centerY(int texture) {
		return (CAMERA_HEIGHT - textures[texture].getHeight()) / 2;
	}
	
	private void clearBoardState() {
		for (int i=0; i<boardState.length; i++) {
			for(int j=0; j< boardState.length; j++) {
				boardState[i][j] = BOARD;
			}
		}
	}
	
	private Scene resetGame() {
		clearBoardState();
		currentPlayer = CROSS;
		gameOver = false;
		
		final Scene scene = new Scene();
		scene.setBackground(new ColorBackground(1f, 1f, 1f));

		final int playerX = centerX(NAUGHT);
		final int playerY = 60;
		
		playerSprite = addSprite(scene, playerX, playerY, textures[currentPlayer]);
		final Sprite reset = new Sprite(centerX(RESET), CAMERA_HEIGHT - textures[RESET].getHeight() - 60, textures[RESET]) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float posX, float posY) {
				TicTacToeActivity.this.scene.setChildScene(resetGame());
				return super.onAreaTouched(pSceneTouchEvent, posX, posY);
			};
		};
		scene.attachChild(reset);
		scene.registerTouchArea(reset);
		
		final int boardX = centerX(BOARD);
		final int boardY = centerY(BOARD);
		final Sprite board = new Sprite(boardX, boardY, textures[BOARD]) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float posX, float posY) {
				
				int x = snap(posX, X);
				int y = snap(posY, Y);
				
				if(validMove(x, y)) {
					applyMove(x, y);
					if(!checkForWinner()){
						switchPlayer();
					} else {
						gameOver = true;
					}
				}	
				return super.onAreaTouched(pSceneTouchEvent, posX, posY);
			}
			
			private boolean checkRow(int y) {
				return boardState[0][y] != BOARD && boardState[0][y] == boardState[1][y] && boardState[0][y] == boardState[2][y];
			}
			
			private boolean checkColumn(int x) {
				return boardState[x][0] != BOARD && boardState[x][0] == boardState[x][1] && boardState[x][0] == boardState[x][2];
			}
			
			private void addLine(int x, int y, int texture) {
				addSprite(scene, x, y, textures[texture]);
			}
			
			private boolean checkForWinner() {
				for(int y=0; y<boardState.length; y++) {
					if(checkRow(y)) {
						addLine(8 + boardX, boardY + coords[Y][y] + textures[CROSS].getHeight()/2 - 5, LINE_HORIZONTAL);
						return true;
					}
				}
				for(int x=0; x<boardState[0].length; x++) {
					if(checkColumn(x)) {
						addLine(boardX + coords[X][x] + textures[CROSS].getWidth()/2 - 5, 8 + boardY , LINE_VERTICAL);
						return true;
					}
				}
				if(boardState[0][0] != BOARD && boardState[0][0] == boardState[1][1] && boardState[0][0] == boardState[2][2]) {
					addLine(8 + boardX, 8 + boardY , LINE_TOP_LEFT);
					return true;
				}
				if(boardState[0][2] != BOARD && boardState[0][2] == boardState[1][1] && boardState[0][2] == boardState[2][0]) {
					addLine(8 + boardX, 8 + boardY , LINE_BOTTOM_LEFT);
					return true;
				}
					
				return false;
			}
			
			private void applyMove(int x, int y) {
				boardState[x][y] = currentPlayer;
				addSprite(this, coords[X][x], coords[Y][y], textures[currentPlayer]);
			}
			
			private boolean validMove(int x, int y) {
				return !gameOver && boardState[x][y] == BOARD;
			}
			
			private void switchPlayer() {
				scene.detachChild(playerSprite);
				currentPlayer = (currentPlayer==NAUGHT) ? CROSS : NAUGHT;
				playerSprite = addSprite(scene, playerX, playerY, textures[currentPlayer]);
			}
			
			private int snap(float n, int axis) {
				if(n < coords[axis][1]) return 0;
				if(n < coords[axis][2]) return 1;
				return 2;
			}
		};
		scene.attachChild(board);
		scene.registerTouchArea(board);
		
		return scene;
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.scene = new Scene();
		scene.setChildScene(resetGame());
		return this.scene;
	}

	@Override
	public void onLoadComplete() {

	}   
}
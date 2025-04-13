package client.gamedata;

import messagesbase.messagesfromserver.EPlayerGameState;

public enum EGameState {
    MUST_ACT,
    MUST_WAIT,
    WON,
    LOST;
    
    public static EGameState fromNetwork(EPlayerGameState state) {
        switch (state) {
            case MustAct: return EGameState.MUST_ACT;
            case MustWait: return EGameState.MUST_WAIT;
            case Won: return EGameState.WON;
            case Lost: return EGameState.LOST;
            default: throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

}

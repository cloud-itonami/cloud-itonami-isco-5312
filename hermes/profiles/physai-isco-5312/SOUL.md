# physai-isco-5312 — 教員補助員（ISCO 5312）の教室支援ロボットの physical-AI bot

私はこの repo（`cloud-itonami/cloud-itonami-isco-5312`、ISCO 5312 教員補助員）に常駐する bot。仕事は 2 つだけ:
**この repo のロボットが物理的にする仕事をシミュレーションして物理量を測ること**と、
**測った結果を根拠に、この repo を 1 反復 1 増分だけ育てること**。

## 何を測っているか

README の Robotics premise: 教室支援ロボットが教材の準備と、監督下での活動ステーションの補助を行い、独立した Teachers Aide Governor がそれを gate する。
その物理的な仕事を `physics.edn`（`itonami.physical-ai.spec.v1`）に宣言し、
`kotoba.robotics.process`（kotoba-lang/robotics）の solver で時間積分して測る。

| case | kind | 何をするか | 判定量 | 限界（basis） |
|---|---|---|---|---|
| `:class-set-to-classroom` | transport | 授業前に 1 クラス分のワークブックを資料室から教室へ運ぶ（70 m、駆動力 60 N）。積荷を掃引 | 1 区間の所要時間 `:cycle-time-s` | 80 s（estimate） |
| `:station-tray-to-desk` | manipulator | 教員のいる場で、活動ステーションのトレー（2 kg）を教材台車から児童の机へ移す。動作時間を掃引 | 肩関節ピークトルク `:peak-tau1-nm` | 30 N·m（estimate） |

測定の入口: `kbb -M:physics`。全 run が数値を返さなければ exit 2 = **測れなかった**（「異常なし」ではない）。
test: `kbb -M:physai-test`（`test/teachers_aide/physics_spec_test.cljk` が physics.edn の妥当性と全 run の計測を検査する。repo の test 全 12 本が kbb の runner で走る）。

## 測って分かったこと・限界（成長の第一候補）

1. **教材の搬送**: 積荷 5〜30 kg では所要時間 71.63 s で変わらない（巡航 1.0 m/s と加速度上限 0.5 m/s² が効く）。約 50 kg から駆動力 60 N が制約になり（50 kg で 71.69 s、80 kg で 72.27 s）、
   限界 80 s を超えるのは積荷 **200.5 kg** —— 教材の重さでは時間は破れない。変わるのはエネルギー（5 kg 635 J → 80 kg 1693 J）。
2. **トレーの移載**: 0.6 s より遅い動作では肩トルクは 25.62 N·m で一定（トレーとアームを支える静的トルクが最大値を決める）。0.4 s で 32.5 N·m。
   限界 30 N·m を守れる最短の動作時間は **0.440 s**。
3. **estimate のままの値**: 区間所要時間 80 s（授業間の休み時間から置き換える）、肩トルク上限 30 N·m（3 kg 級協働ロボットの仕様書と、人と接触しうる場合の力の上限を定めた規格で置き換える）、
   アームの寸法・質量、搬送ロボットの駆動力・転がり抵抗係数。

## 1 反復の手順（成長 tick）

evidence（prompt に注入される）を読み、次の順で **1 つだけ** 選ぶ:

1. evidence が `TESTS-FAIL` / `PROBE-UNMEASURED` → それを直す（最小の差分）。
2. `physics.edn` の `:basis "estimate: ..."` を 1 つ、出典のある値（規格番号・メーカー仕様・法令の条番号と URL）に置き換える。
   出典が取れなければ置き換えない —— 推測で `estimate` を外さない。
3. この業種・職種のロボットがする別の物理的な仕事を 1 case 足す（`:kind` は :transport / :manipulator / :material /
   :thermal / :tank-drain / :pipe-flow）。README の premise と docs から根拠を取る。
4. governor が同じ solver で独立に再計算して、限界を超える action を止める純関数と test を足す（大きい変更。1〜3 が尽きてから）。

作業の仕方（これ以外の経路で main に入れない）:

```
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk branch physai-isco-5312 <slug>   # worktree を切る（path を印字）
# その worktree で編集 → kbb -M:physai-test → kbb -M:physics → git commit
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk land physai-isco-5312 <branch>   # 検証して merge
```

`land` が検証すること: test 数・assertion 数が main より減っていない、fail/error 0、probe が
`:count = :expected` で sweep も縮んでいない。通らなければ merge しない —— そのときは理由を報告して終える。

## 守ること

- **main に直接 push しない。force-push しない。rebase しない。** 着地は `land` だけ。
- **test を弱めて緑にしない**（assert を消す・sweep を減らす・限界を緩めて合格させる）。`land` は数の減少を拒否する。
- **数値を捏造しない。** 物理量は solver が出したものだけ。`:basis` は出典か `estimate:` のどちらかを必ず書く。
- **実機を動かさない。** これはシミュレーションと governor の repo。`:high` / `:safety-critical` な actuation は
  人の承認なしに commit されない設計を崩さない。
- この repo 以外（kotoba-lang/robotics の solver を含む）は編集しない。solver に足りないものは報告に書く。
- 1 反復で終える。報告は: 選んだ候補 / 変えたこと / test 数の前後 / probe の主要量の前後 / land の結果。誇張しない。

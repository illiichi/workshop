(ns workshop.core)
;; This is a tutorial to learn the basis of making sound with Overtone.
;;
;; Only limited functionality of Overtone are used to make a explanation simple.
;; It assumes that you understand basic Clojure syntax.


;; load overtone first
(use 'overtone.live)

;; sine wave
;; ---------
(comment (image images/sin.png))
;; play sine wave at 440Hz in 3 sec.
;; (osc means oscillator)
(demo 3 (sin-osc 440))

;; change volume by multiplication
(demo 3 (* 1/2 (sin-osc 440)))

;; CAUTION:
;; When you evaluate expression below outside 'demo' macro:
;;
;; (* 1/2 (sin-osc 440))
;;
;; you will get this error:
;; Unhandled java.lang.ClassCastException
;;    overtone.sc.machinery.ugen.sc_ugen.SCUGen cannot be cast to java.lang.Number
;;
;; Waves arithmetic is supported by Overtone's macro.


(comment (image images/multiply_sin.png))
;; change volume over time
(demo 5 (* (sin-osc 0.4) (sin-osc 440)))

;; change frequency over time
(demo 3 (sin-osc (+ 440 (* 4 (sin-osc 1)))))

;; you can use local variable
(demo 3 (let [freq (+ 440 (* 220 (sin-osc 0.2)))]
          (sin-osc freq)))

;; sum of sine waves
(demo 3 (let [freq 440]
          (->> (range 1 8)
               (map #(* freq %)) ; integer multiples of the fundamental frequency = harmonic
               (map sin-osc)
               (apply +))))

;; [For users who not familiar with Clojure]
;; the code above is same as below
;;
;; (safe-demo 3 (let [freq 440]
;;           (apply + (map sin-osc (map (fn [x] (* freq x)) (range 1 8))))))

;; clipping
;; --------

;; clip signal. The output will be:
;;   1 if it's greater than  1
;;  -1 if it's greater than -1
(demo 3 (clip:ar (* 2 (sin-osc 440)) -1 1))

;; define macro to avoid the dangerous volume sound
(defmacro safe-demo [demo-time body]
  `(demo ~demo-time
         (clip:ar ~body -1 1)))

;; now you can use `safe-demo`
(safe-demo 3 (* 2 (sin-osc 440)))

;; envelope
;; --------

(comment (image images/env_lin.png))
;; envelope is a line which are concatenated several segments of line
;; 
;; (envelope [0 1 0] [0.05 0.5]) expresses a envelope:
;;   first increasing from 0 to 1 in 0.05 sec
;;   then  decreasing from 1 to 0 in 0.5 sec
;; `env-gen` make envelope into wave.


;; change volume using a envelope
(safe-demo 3 (let [freq 880
                   vol (env-gen (envelope [0 1 0] [0.05 0.5]))]
               (* vol (sin-osc freq))))

;; change frequency using a envelope
(safe-demo 3 (let [freq (env-gen (envelope [440 880 440 3520] [0.1 0.5 3]))]
               (sin-osc freq)))

;; envelope will be fired when 2nd argument of `env-gen` changes from 1 to 0.
;; (impulse 1) is trigger at 1Hz
(safe-demo 3 (let [gate (impulse 1)
                   freq 880
                   vol (env-gen (envelope [0 1 0] [0.05 0.5]) gate)]
               (* vol (sin-osc freq))))


(comment (image images/env_quad.png))
;; specify line shape of envelope, the third argument of `env-gen`
;; -4 means the segment curve goes down as x^4
(safe-demo 3 (let [freq 880
                   vol (env-gen (envelope [0 1 0] [0.01 0.5] [0 -4]))]
               (* vol (sin-osc freq))))

(comment (image images/env_sin.png))
(safe-demo 3 (let [freq 880
                   vol (env-gen (envelope [0 1 1 0] [0.2 0.3 0.7] [:sine 0 -4]))]
               (* vol (sin-osc freq))))

(comment (image images/env_step.png))
;; example for discontinuous frequency change
;; :step make immediately jump to final value
(safe-demo 3 (let [freq (env-gen (envelope [900 900 1300] [0.1 0.5] :step))
                   vol  (env-gen (envelope [0 1 0] [0 0.6]))]
               (* vol (sin-osc freq))))

(comment (image images/env_step_2.png))
;; another example
(safe-demo 3 (let [freq (env-gen (envelope [440.0 440.0 880.0] [0.1 0.4]))
                   vol  (env-gen (envelope [1 0] [0.5]))]
               (* vol (sin-osc freq))))

;; white-noise and filter
;; ---------

;; (white-noise) generate from -1 to 1 randomly.
;; As result, it contains all frequencies
(safe-demo 3 (white-noise))

;; multiply the envelope
(safe-demo 3 (let [vol (env-gen (envelope [0 1 0] [0.05 0.2] -4))]
               (* vol (white-noise))))

;; filtering
(safe-demo 3 (lpf (white-noise) 1000))       ; low pass filter
(safe-demo 3 (hpf (white-noise) 1000))       ; high pass filter
(safe-demo 3 (bpf (white-noise) 1000 0.1))   ; band pass filter (if 3rd argument is smaller, the band is narrower)

;; resonant low pass filter. The sound is emphasized around 1000Hz
;; (if 3rd argument is smaller, more emphasizing)
(safe-demo 3 (rlpf (white-noise) 1000 0.1))

;; change volume and frequency by envelope
(safe-demo 3 (let [freq (env-gen (envelope [3000 880] [0.025]))
                   vol  (env-gen (envelope [0 1 0] [0 0.5]))]
               (* vol (rlpf (white-noise) freq))))


;; stereo
;; ---------

;; passing sequence with legth 2,
;; first element is used for left channel
;; second element is used for right channel
(safe-demo 3 (map sin-osc [440 660]))

;; second argment of impulse is phase offset ranged from 0 to 1
(safe-demo 3 (map #(* (sin-osc %1)
                      (env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 %2)))
                  [440 660]
                  [0 1/2]))

;; Splay spreads an array of channels across the stereo field
(safe-demo 8 (splay (map #(* 4 (sin-osc %1)
                             (env-gen (envelope [0 1 0] [0.01 0.5]) (impulse 1/2 %2)))
                         (->> 400
                              (iterate #(* 1.4 %))
                              (take 10))
                         (range 0 1 0.1))))

;; exercise
;; ---------

;; try making your sound to be:
;; - more complecated
;; - more noisy
;; - closed to real acourstic
;; - like strange instruments

;; some example of expressions to complex sound
(shuffle (range 100 1000 100))
(take 8 (iterate #(* 1.1 %) 100))       ;don't forget "take".
(->> (reductions * 1000 (cycle [5/4 7/3 11/10 3/5 4/7]))
     (take 20))

(safe-demo (reduce (fn [acc x] (sin-osc (* x acc)))
                   (sin-osc 10)
                   (range 1000 5000 1000)))

;; advanced topics
;; ---------

(comment
  ;; if SuperCollider is running, you can connect to it instead of use overtone.live.
  ;; You can see the output wave by evalate 's.scope' in SC,
  ;; or frequency by evaluate 's.freqscope' in SC
  (use 'overtone.core)
  (connect-external-server "localhost" 57110))


;; "multichannel expansion" is supported so that the two codes below are the same
(safe-demo 3 (splay
              (map *
                   (map sin-osc (range 500 1500 100))
                   (map #(env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 %))
                        (range 0 1 0.1)))))

(safe-demo 3 (splay
              (* (sin-osc (range 500 1500 100))
                 (env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 (range 0 1 0.1))))))

;; 'demo' macro release resouces after certain seconds, You can manage the life time of sound.
;; In this example, resouce will be released after 0.55 seconds.
(demo 3 (let [freq (env-gen (envelope [3000 440] [0.1]))
              vol  (env-gen (envelope [0 1 0] [0.05 0.5]) :action FREE)]
          (* vol (sin-osc freq))))


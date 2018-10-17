(ns workshop.core)
;; This is a tutorial to learn the basis of making sound with Overtone.
;;
;; It assumes that you understand basic Clojure syntax.
;; The use of Overtone functionality is limited as possible to simplify the explanation.


;; Load overtone first
(use 'overtone.live)

;; sine wave
;; ---------

(comment (image images/sin.png))
;; Play sine wave at 440Hz in 2 sec.
;; If frequency is increasing, the sound become higher.
(demo 2 (sin-osc 440))

;; Change amplitude by multiplication.
;; Sin wave is ranged from -1 to 1.
;; If amlitude is decreasing, the volume of sound become smaller.
(demo 2 (* 1/2 (sin-osc 440)))

;; CAUTION:
;; Waves arithmetic is supported by Overtone's macro.
;; When you evaluate the expression below outside of 'demo' macro:
;;
;; (* 1/2 (sin-osc 440))
;;
;; You will get this error:
;;
;; Unhandled java.lang.ClassCastException
;;    overtone.sc.machinery.ugen.sc_ugen.SCUGen cannot be cast to java.lang.Number

(comment (image images/multiply_sin.png))
;; Change amplitude over time.
;; The volume of sound is changing over time.
;; If volume changing is more rapid, it will be difficult to recognize volume changing,
;; then you feel it as changing the timber.
;; Not only the structure of expression is important, but also the number itself is important.
(demo 4 (* (sin-osc 0.4) (sin-osc 440)))

;; Change frequency over time
;; You would feel the sound is vibrating.
;; You can increase amount of vibration or make more rapid
(demo 2 (sin-osc (+ 440 (* 2 (sin-osc 1)))))

;; you can use local variable
(demo 2 (let [freq (+ 440 (* 220 (sin-osc 0.2)))]
          (sin-osc freq)))

;; sum of sine waves
(demo 2 (let [freq 440]
          (->>                          ; using threading macro
           (range 1 8)                  ; make integers from 1 to 7
           (map #(* freq %))            ; make integer multiples of the fundamental frequency
                                        ;  = harmonic frequencies
           (map sin-osc)                ; map frequencies into sin-osc
           (apply +))))                 ; sum up waves

;; --- For non-Clojurians ---
;; -> or ->> are used to change the order.
;;
;; The code above is same as below:
;;
;; (demo 2 (let [freq 440]
;;           (apply +
;;                  (map sin-osc
;;                       (map (fn [x] (* freq x))
;;                            (range 1 8))))))
;; --------------------------------------------

;; harmonic frequencies are essential for the sound of instruments
;; such as plucking the string or blowing the pipe
;;
;; You can read the explanation in detail from:
;; https://www.soundonsound.com/techniques/synthesizing-wind-instruments


;; clipping
;; --------

;; Clip signal. The output will be:
;;   1 if it's greater than  1
;;  -1 if it's greater than -1
(demo 2 (->      ; be careful that this is '->' not '->>' in this case
         (* 2 (sin-osc 440))  ; this expression is ranged from -2 to 2
         (clip:ar -1 1)))               ; clip over 1 or under -1

;; Define macro to avoid the dangerous volume sound
(defmacro safe-demo [demo-time body]
  `(demo ~demo-time
         (clip:ar ~body -1 1)))

;; Now you can use `safe-demo`
(safe-demo 2 (* 2 (sin-osc 440)))

;; envelope
;; --------

(comment (image images/env_lin.png))
;; Envelope is a line which are concatenated several segments of line
;;
;; (envelope [0 1 0] [0.05 0.5]) expresses a envelope:
;;   first increasing from 0 to 1 in 0.05 sec
;;   then  decreasing from 1 to 0 in 0.5 sec
;; `env-gen` make envelope into wave.


;; Multiply by envelope
(safe-demo 2 (let [freq 880
                   amp (env-gen (envelope [0 1 0] [0.05 0.5]))]
               (* amp (sin-osc freq))))

;; Guess when you hit something.
;; First, hitting energy change into sound,
;; then the volume of sound become loud quickly,
;; then the sound diverge gradually.

;; The standing duration is smaller, the sound become harder.
;; The relasing duration is smaller, the sound echo less.

(comment (image images/env_quad.png))
;; Specify line shape of envelope, the third argument of `envelope`
;; 0 means linear,
;; positive number means concave down,
;; negative number means concave up.
;; The absolute value becomes greater, the curve become more steep.
(safe-demo 2 (let [freq 880
                   amp (env-gen (envelope [0 1 0] [0.01 0.5] [0 -4]))]
               (* amp (sin-osc freq))))

(comment (image images/env_sin.png))
;; You can use `:sine` to specify the shape.
;;
;; The blowing sound needs duration to become stable.
;; After it get stable, the sound keep going during blowing.
(safe-demo 2 (let [freq 880
                   amp (env-gen (envelope [0 1 1 0] [0.2 0.3 0.7] [:sine 0 -4]))]
               (* amp (sin-osc freq))))

;; You can use a envelope to change frequency.
;; And envelope has more than two segments.
(safe-demo 2 (let [freq (env-gen (envelope [440 880 440 3520] [0.1 0.5 3]))]
               (sin-osc freq)))

(comment (image images/env_step.png))
;; example for discontinuous frequency change
(safe-demo 2 (let [freq (env-gen (envelope [900 900 1300]
                                           [0.1 0.5]
                                           :step)) ; :step make immediately jump to final value
                   amp  (env-gen (envelope [0 1 0] [0 0.6]))]
               (* amp (sin-osc freq))))

(comment (image images/env_step_2.png))
;; another example
(safe-demo 2 (let [freq (env-gen (envelope [440.0 440.0 880.0] [0.1 0.4]))
                   amp  (env-gen (envelope [1 0] [0.5]))]
               (* amp (sin-osc freq))))

;; fire envelope several times
(safe-demo 4 (let [gate (impulse 1)     ; this is trigger at 1Hz
                   freq 880
                                        ; envelope will be fired when 2nd argument of `env-gen` changes from 1 to 0.
                   amp (env-gen (envelope [0 1 0] [0.05 0.5]) gate)]
               (* amp (sin-osc freq))))


;; white-noise and filter
;; ---------

;; (white-noise) generate from -1 to 1 randomly.
;; As result, it contains all frequencies
(safe-demo 2 (white-noise))

;; Multiply the envelope.
;; The sound is like percussion
(safe-demo 2 (let [amp (env-gen (envelope [0 1 0] [0.05 0.2] -4))]
               (* amp (white-noise))))

;; Filtering
(safe-demo 2 (lpf (white-noise) 1000))       ; low pass filter
(safe-demo 2 (hpf (white-noise) 1000))       ; high pass filter
(safe-demo 2 (bpf (white-noise) 1000 0.1))   ; band pass filter (if 3rd argument is smaller, the band is narrower)

;; Resonant low pass filter. The sound is emphasized around 1000Hz
;; (if 3rd argument is smaller, more emphasizing)
(safe-demo 2 (rlpf (white-noise) 1000 0.1))

;; Change amplitude and frequency by envelope
;; Note that first 0.025 seconds could make big difference.
(safe-demo 2 (let [freq (env-gen (envelope [3000 800] [0.025]))
                   amp  (env-gen (envelope [0 1 0] [0 0.5]))]
               (* amp (rlpf (white-noise) freq))))


;; stereo
;; ---------

;; passing sequence with legth 2,
;; first element is used for left channel
;; second element is used for right channel
(safe-demo 2 (map sin-osc [440 660]))

;; second argment of impulse is phase offset ranged from 0 to 1
(safe-demo 2 (map #(* (sin-osc %1)
                      (env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 %2)))
                  [440 660]
                  [0 1/2]))

;; another example with streo
;; the sound is moving around
(safe-demo 4 (map #(* (sin-osc 440)
                      (sin-osc 1/6 %)) ; second argment of sin-osc is phase offset ranged from 0 to 2π
                  [0 (* 1/2 Math/PI)]))

;; `splay' spreads an array of channels across the stereo field
(safe-demo 8 (splay
              (map #(* 4 (sin-osc %1)                           ; first argument is for frequency
                       (env-gen (envelope [0 1 0] [0.01 2] -4)
                                (impulse 1/2 %2)))              ; second argument is for phase

                   (iterate #(* 1.5 %) 400)                     ; sequence of freqencies

                   (->> (range 0 1 1/10)                        ; sequence of phase
                        (rotate 1)
                        reverse))))

;; exercise
;; ---------

;; try making your sound to be:
;; - more complecated
;; - more noisy
;; - closed to real acourstic
;; - like strange instruments

;; some examples of expressions to complex sound
(shuffle (range 100 1000 100))

(take 8 (iterate #(* 1.1 %) 100))       ;don't forget "take".

(->> (reductions * 1000 (cycle [5/4 7/3 11/10 3/5 4/7]))
     (take 20))

(safe-demo 1 (reduce (fn [acc x] (sin-osc (* x acc)))
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
(safe-demo 2 (splay
              (map *
                   (map sin-osc (range 500 1500 100))
                   (map #(env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 %))
                        (range 0 1 0.1)))))

(safe-demo 2 (splay
              (* (sin-osc (range 500 1500 100))
                 (env-gen (envelope [0 1 0] [0.05 0.5]) (impulse 1 (range 0 1 0.1))))))

;; 'demo' macro release resouces after certain seconds, You can manage the life time of sound.
;; In this example, resouce will be released after 0.55 seconds.
(demo 2 (let [freq (env-gen (envelope [3000 440] [0.1]))
              amp  (env-gen (envelope [0 1 0] [0.05 0.5]) :action FREE)]
          (* amp (sin-osc freq))))


(defun open-image--detect-file-path ()
  (save-excursion
    (beginning-of-defun)
    (re-search-forward "(comment (image ")
    (thing-at-point 'sexp t)))

(defun open-image--fix-to-absolute-path (path)
  (s-concat default-directory "../../" path))

(defun open-image--detect-width ()
  (-let (((x1 y1 x2 y2) (window-inside-pixel-edges
                         (get-buffer-window (current-buffer)))))
    (/ (- x2 x1) 2)))

(defun open-image--replace-image (start end path)
  (put-text-property
   start (1- end)
   'display
   (cons 'image
         `(:type imagemagick
                 :file ,path
                 :width ,(open-image--detect-width)))))

(defun open-image ()
  (interactive)
  (-let (((s . e) (bounds-of-thing-at-point 'defun)))
    (open-image--replace-image
     s e
     (open-image--fix-to-absolute-path (open-image--detect-file-path)))))

(define-key clojure-mode-map (kbd "s-n") 'open-image)


(defun change-mini-buffer-font-size ()
  (dolist
      (buf (list " *Minibuf-0*" " *Minibuf-1*" " *Echo Area 0*" " *Echo Area 1*" "*Quail Completions*"))
    (when (get-buffer buf)
      (with-current-buffer buf
        (setq-local face-remapping-alist '((default (:height 2.0))))))))

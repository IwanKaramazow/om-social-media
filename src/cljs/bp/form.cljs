(ns bp.form
  (:require [om.next :as om]))

(defn handleChange [c id field changeEvent]
  "Given a component, the id of the form, the field that needs to be updated
   and the event, update the state with the value of that event."
  (om/transact! c `[(form/update
                        {:form/id ~id
                         :form/field ~field
                         :form/value ~(.. changeEvent -target -value)})]))

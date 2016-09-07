// Compiled by ClojureScript 1.9.227 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
goog.require('cljs.spec');
cljs.repl.print_doc = (function cljs$repl$print_doc(p__38498){
var map__38523 = p__38498;
var map__38523__$1 = ((((!((map__38523 == null)))?((((map__38523.cljs$lang$protocol_mask$partition0$ & (64))) || (map__38523.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__38523):map__38523);
var m = map__38523__$1;
var n = cljs.core.get.call(null,map__38523__$1,new cljs.core.Keyword(null,"ns","ns",441598760));
var nm = cljs.core.get.call(null,map__38523__$1,new cljs.core.Keyword(null,"name","name",1843675177));
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,[cljs.core.str((function (){var temp__4657__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__4657__auto__)){
var ns = temp__4657__auto__;
return [cljs.core.str(ns),cljs.core.str("/")].join('');
} else {
return null;
}
})()),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__38525_38547 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__38526_38548 = null;
var count__38527_38549 = (0);
var i__38528_38550 = (0);
while(true){
if((i__38528_38550 < count__38527_38549)){
var f_38551 = cljs.core._nth.call(null,chunk__38526_38548,i__38528_38550);
cljs.core.println.call(null,"  ",f_38551);

var G__38552 = seq__38525_38547;
var G__38553 = chunk__38526_38548;
var G__38554 = count__38527_38549;
var G__38555 = (i__38528_38550 + (1));
seq__38525_38547 = G__38552;
chunk__38526_38548 = G__38553;
count__38527_38549 = G__38554;
i__38528_38550 = G__38555;
continue;
} else {
var temp__4657__auto___38556 = cljs.core.seq.call(null,seq__38525_38547);
if(temp__4657__auto___38556){
var seq__38525_38557__$1 = temp__4657__auto___38556;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__38525_38557__$1)){
var c__37162__auto___38558 = cljs.core.chunk_first.call(null,seq__38525_38557__$1);
var G__38559 = cljs.core.chunk_rest.call(null,seq__38525_38557__$1);
var G__38560 = c__37162__auto___38558;
var G__38561 = cljs.core.count.call(null,c__37162__auto___38558);
var G__38562 = (0);
seq__38525_38547 = G__38559;
chunk__38526_38548 = G__38560;
count__38527_38549 = G__38561;
i__38528_38550 = G__38562;
continue;
} else {
var f_38563 = cljs.core.first.call(null,seq__38525_38557__$1);
cljs.core.println.call(null,"  ",f_38563);

var G__38564 = cljs.core.next.call(null,seq__38525_38557__$1);
var G__38565 = null;
var G__38566 = (0);
var G__38567 = (0);
seq__38525_38547 = G__38564;
chunk__38526_38548 = G__38565;
count__38527_38549 = G__38566;
i__38528_38550 = G__38567;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_38568 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__36351__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__36351__auto__)){
return or__36351__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_38568);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_38568)))?cljs.core.second.call(null,arglists_38568):arglists_38568));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/"),cljs.core.str(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/special_forms#"),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__38529_38569 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__38530_38570 = null;
var count__38531_38571 = (0);
var i__38532_38572 = (0);
while(true){
if((i__38532_38572 < count__38531_38571)){
var vec__38533_38573 = cljs.core._nth.call(null,chunk__38530_38570,i__38532_38572);
var name_38574 = cljs.core.nth.call(null,vec__38533_38573,(0),null);
var map__38536_38575 = cljs.core.nth.call(null,vec__38533_38573,(1),null);
var map__38536_38576__$1 = ((((!((map__38536_38575 == null)))?((((map__38536_38575.cljs$lang$protocol_mask$partition0$ & (64))) || (map__38536_38575.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__38536_38575):map__38536_38575);
var doc_38577 = cljs.core.get.call(null,map__38536_38576__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_38578 = cljs.core.get.call(null,map__38536_38576__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_38574);

cljs.core.println.call(null," ",arglists_38578);

if(cljs.core.truth_(doc_38577)){
cljs.core.println.call(null," ",doc_38577);
} else {
}

var G__38579 = seq__38529_38569;
var G__38580 = chunk__38530_38570;
var G__38581 = count__38531_38571;
var G__38582 = (i__38532_38572 + (1));
seq__38529_38569 = G__38579;
chunk__38530_38570 = G__38580;
count__38531_38571 = G__38581;
i__38532_38572 = G__38582;
continue;
} else {
var temp__4657__auto___38583 = cljs.core.seq.call(null,seq__38529_38569);
if(temp__4657__auto___38583){
var seq__38529_38584__$1 = temp__4657__auto___38583;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__38529_38584__$1)){
var c__37162__auto___38585 = cljs.core.chunk_first.call(null,seq__38529_38584__$1);
var G__38586 = cljs.core.chunk_rest.call(null,seq__38529_38584__$1);
var G__38587 = c__37162__auto___38585;
var G__38588 = cljs.core.count.call(null,c__37162__auto___38585);
var G__38589 = (0);
seq__38529_38569 = G__38586;
chunk__38530_38570 = G__38587;
count__38531_38571 = G__38588;
i__38532_38572 = G__38589;
continue;
} else {
var vec__38538_38590 = cljs.core.first.call(null,seq__38529_38584__$1);
var name_38591 = cljs.core.nth.call(null,vec__38538_38590,(0),null);
var map__38541_38592 = cljs.core.nth.call(null,vec__38538_38590,(1),null);
var map__38541_38593__$1 = ((((!((map__38541_38592 == null)))?((((map__38541_38592.cljs$lang$protocol_mask$partition0$ & (64))) || (map__38541_38592.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__38541_38592):map__38541_38592);
var doc_38594 = cljs.core.get.call(null,map__38541_38593__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_38595 = cljs.core.get.call(null,map__38541_38593__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_38591);

cljs.core.println.call(null," ",arglists_38595);

if(cljs.core.truth_(doc_38594)){
cljs.core.println.call(null," ",doc_38594);
} else {
}

var G__38596 = cljs.core.next.call(null,seq__38529_38584__$1);
var G__38597 = null;
var G__38598 = (0);
var G__38599 = (0);
seq__38529_38569 = G__38596;
chunk__38530_38570 = G__38597;
count__38531_38571 = G__38598;
i__38532_38572 = G__38599;
continue;
}
} else {
}
}
break;
}
} else {
}

if(cljs.core.truth_(n)){
var temp__4657__auto__ = cljs.spec.get_spec.call(null,cljs.core.symbol.call(null,[cljs.core.str(cljs.core.ns_name.call(null,n))].join(''),cljs.core.name.call(null,nm)));
if(cljs.core.truth_(temp__4657__auto__)){
var fnspec = temp__4657__auto__;
cljs.core.print.call(null,"Spec");

var seq__38543 = cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"args","args",1315556576),new cljs.core.Keyword(null,"ret","ret",-468222814),new cljs.core.Keyword(null,"fn","fn",-1175266204)], null));
var chunk__38544 = null;
var count__38545 = (0);
var i__38546 = (0);
while(true){
if((i__38546 < count__38545)){
var role = cljs.core._nth.call(null,chunk__38544,i__38546);
var temp__4657__auto___38600__$1 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__4657__auto___38600__$1)){
var spec_38601 = temp__4657__auto___38600__$1;
cljs.core.print.call(null,[cljs.core.str("\n "),cljs.core.str(cljs.core.name.call(null,role)),cljs.core.str(":")].join(''),cljs.spec.describe.call(null,spec_38601));
} else {
}

var G__38602 = seq__38543;
var G__38603 = chunk__38544;
var G__38604 = count__38545;
var G__38605 = (i__38546 + (1));
seq__38543 = G__38602;
chunk__38544 = G__38603;
count__38545 = G__38604;
i__38546 = G__38605;
continue;
} else {
var temp__4657__auto____$1 = cljs.core.seq.call(null,seq__38543);
if(temp__4657__auto____$1){
var seq__38543__$1 = temp__4657__auto____$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__38543__$1)){
var c__37162__auto__ = cljs.core.chunk_first.call(null,seq__38543__$1);
var G__38606 = cljs.core.chunk_rest.call(null,seq__38543__$1);
var G__38607 = c__37162__auto__;
var G__38608 = cljs.core.count.call(null,c__37162__auto__);
var G__38609 = (0);
seq__38543 = G__38606;
chunk__38544 = G__38607;
count__38545 = G__38608;
i__38546 = G__38609;
continue;
} else {
var role = cljs.core.first.call(null,seq__38543__$1);
var temp__4657__auto___38610__$2 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__4657__auto___38610__$2)){
var spec_38611 = temp__4657__auto___38610__$2;
cljs.core.print.call(null,[cljs.core.str("\n "),cljs.core.str(cljs.core.name.call(null,role)),cljs.core.str(":")].join(''),cljs.spec.describe.call(null,spec_38611));
} else {
}

var G__38612 = cljs.core.next.call(null,seq__38543__$1);
var G__38613 = null;
var G__38614 = (0);
var G__38615 = (0);
seq__38543 = G__38612;
chunk__38544 = G__38613;
count__38545 = G__38614;
i__38546 = G__38615;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
} else {
return null;
}
}
});

//# sourceMappingURL=repl.js.map